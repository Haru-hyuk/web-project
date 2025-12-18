package com.wordweb.service;

import com.wordweb.entity.ClusterWord;
import com.wordweb.entity.Word;
import com.wordweb.repository.ClusterWordRepository;
import com.wordweb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ClusterWordService {

    @Value("${deepseek.api-key}")
    private String deepseekApiKey;

    private static final String DEEPSEEK_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final int DEFAULT_TOP_N = 10;
    private static final double DEFAULT_THRESHOLD = 0.5;

    private final ClusterWordRepository clusterWordRepository;
    private final WordRepository wordRepository;
    private final EmbeddingService embeddingService;

    // 중복 생성 방지를 위한 동시성 제어 (wordId로 락)
    private final ConcurrentHashMap<Long, Boolean> creatingLocks = new ConcurrentHashMap<>();

    // 비동기 처리를 위한 스레드 풀
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 특정 단어와 유사한 단어 찾기 (유사도 기반)
     * @param centerWordId 중심 단어 ID
     * @param forceRegenerate 기존 클러스터가 있어도 강제 재생성 여부 (기본값: false)
     */
    @Transactional
    public List<ClusterWord> createCluster(Long centerWordId, boolean forceRegenerate) {
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다"));

        // 기존 클러스터 확인 (모든 사용자가 공유)
        List<ClusterWord> existingClusters = clusterWordRepository.findByCenterWord(centerWord);

        // 기존 클러스터가 있고 강제 재생성이 아니면 기존 데이터 반환 (성능 최적화)
        if (!existingClusters.isEmpty() && !forceRegenerate) {
            return existingClusters;
        }

        // 중복 생성 방지: 동일한 단어로 이미 생성 중이면 대기
        Long lockKey = centerWordId;

        // 이미 생성 중이면 기존 결과 반환
        if (creatingLocks.putIfAbsent(lockKey, Boolean.TRUE) != null) {
            // 이미 다른 요청이 생성 중 - 기존 클러스터 반환
            if (!existingClusters.isEmpty()) {
                return existingClusters;
            }
            // 생성 중이지만 아직 완료 안됨 - 빈 리스트 반환
            return new ArrayList<>();
        }

        try {
            // 강제 재생성인 경우에만 기존 클러스터 삭제
            if (forceRegenerate && !existingClusters.isEmpty()) {
                clusterWordRepository.deleteAll(existingClusters);
                clusterWordRepository.flush(); // 즉시 삭제 반영
            }

            List<ClusterWord> clusters = new ArrayList<>();
            Set<Long> addedWordIds = new HashSet<>();
            addedWordIds.add(centerWordId); // 중심 단어는 제외

            int synonymCount = 0;
            int antonymCount = 0;

            // DeepSeek API로 중심 단어의 유의어/반의어 찾기 (우선 처리)
            try {
                WordRelations relations = getWordRelationsFromDeepSeek(centerWord.getWord());

                // 유의어 추가 (반드시 포함되도록 우선 처리, 최대 10개 제한)
                for (String synonym : relations.getSynonyms()) {
                    if (clusters.size() >= DEFAULT_TOP_N) break; // 최대 10개 제한

                    // 대소문자 구분 없이 검색 (소문자로 통일)
                    Word synonymWord = wordRepository.findByWord(synonym.toLowerCase()).orElse(null);
                    if (synonymWord != null && !addedWordIds.contains(synonymWord.getWordId())) {
                        ClusterWord cluster = ClusterWord.create(centerWord, synonymWord, 0.9, "synonym");
                        clusters.add(cluster);
                        addedWordIds.add(synonymWord.getWordId());
                        synonymCount++;
                    }
                }

                // 반의어 추가 (반드시 포함되도록 우선 처리, 최대 10개 제한)
                for (String antonym : relations.getAntonyms()) {
                    if (clusters.size() >= DEFAULT_TOP_N) break; // 최대 10개 제한

                    // 대소문자 구분 없이 검색 (소문자로 통일)
                    Word antonymWord = wordRepository.findByWord(antonym.toLowerCase()).orElse(null);
                    if (antonymWord != null && !addedWordIds.contains(antonymWord.getWordId())) {
                        ClusterWord cluster = ClusterWord.create(centerWord, antonymWord, 0.7, "antonym");
                        clusters.add(cluster);
                        addedWordIds.add(antonymWord.getWordId());
                        antonymCount++;
                    }
                }

            } catch (Exception e) {
                System.err.println("DeepSeek API 호출 실패 (유의어/반의어 조회): " + e.getMessage());
            }

            // 유사도 기반 단어 추가 (임베딩이 있는 경우에만)
            int similarityCount = 0;
            if (centerWord.getEmbedding() != null) {
                double[] centerEmbedding = embeddingService.parseEmbedding(centerWord.getEmbedding());
                List<ClusterWord> similarityClusters = new ArrayList<>();

                // 임베딩이 있는 단어만 조회하여 성능 최적화 (기존: findAll() -> 개선: findAllWithEmbedding())
                wordRepository.findAllWithEmbedding().forEach(word -> {
                    if (addedWordIds.contains(word.getWordId())) {
                        return;
                    }

                    double[] embedding = embeddingService.parseEmbedding(word.getEmbedding());
                    double similarity = embeddingService.cosineSimilarity(centerEmbedding, embedding);

                    if (similarity >= DEFAULT_THRESHOLD) {
                        ClusterWord cluster = ClusterWord.create(centerWord, word, similarity, "similarity");
                        similarityClusters.add(cluster);
                        addedWordIds.add(word.getWordId());
                    }
                });

                // 유사도 순으로 정렬
                similarityClusters.sort(Comparator.comparingDouble(ClusterWord::getScore).reversed());

                // 남은 공간만큼만 추가 (최대 10개 유지)
                int remainingSlots = DEFAULT_TOP_N - clusters.size();
                if (remainingSlots > 0) {
                    int limit = Math.min(remainingSlots, similarityClusters.size());
                    clusters.addAll(similarityClusters.subList(0, limit));
                    similarityCount = limit;
                }
            }

            // 저장 전 중복 체크 강화 (DB 레벨 중복 방지)
            List<ClusterWord> clustersToSave = new ArrayList<>();
            for (ClusterWord cluster : clusters) {
                // DB에 이미 존재하는지 확인
                if (!clusterWordRepository.existsByCenterWordAndRelatedWord(
                        centerWord, cluster.getRelatedWord())) {
                    clustersToSave.add(cluster);
                }
            }

            // 중복이 제거된 클러스터만 저장
            List<ClusterWord> savedClusters = clusterWordRepository.saveAll(clustersToSave);

            return savedClusters;
        } finally {
            // 락 해제
            creatingLocks.remove(lockKey);
        }
    }


    /**
     * 특정 단어와 유사한 단어 찾기 (유사도 기반) - 기본 메서드 (재생성 안함)
     */
    @Transactional
    public List<ClusterWord> createCluster(Long centerWordId) {
        return createCluster(centerWordId, false);
    }

    /**
     * 비동기로 클러스터 생성 (백그라운드 처리)
     * 기존 클러스터가 있으면 즉시 반환하고, 없으면 백그라운드에서 생성
     */
    public CompletableFuture<List<ClusterWord>> createClusterAsync(Long centerWordId) {
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다"));

        // 기존 클러스터 확인 (모든 사용자가 공유)
        List<ClusterWord> existingClusters = clusterWordRepository.findByCenterWord(centerWord);

        // 기존 클러스터가 있으면 즉시 반환
        if (!existingClusters.isEmpty()) {
            return CompletableFuture.completedFuture(existingClusters);
        }

        // 없으면 백그라운드에서 생성
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createCluster(centerWordId, false);
            } catch (Exception e) {
                System.err.println("비동기 클러스터 생성 실패: " + e.getMessage());
                return new ArrayList<>();
            }
        }, executorService);
    }

    /** 특정 중심 단어에 대한 클러스터 추가 */
    public void addCluster(Long centerWordId, Long relatedWordId, Double score, String type) {
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("기준 단어를 찾을 수 없습니다."));

        Word relatedWord = wordRepository.findById(relatedWordId)
                .orElseThrow(() -> new RuntimeException("연관 단어를 찾을 수 없습니다."));

        // 중복 방지
        if (clusterWordRepository.existsByCenterWordAndRelatedWord(centerWord, relatedWord)) {
            return; // 이미 존재하면 아무것도 안 함 (idempotent)
        }

        clusterWordRepository.save(ClusterWord.create(centerWord, relatedWord, score, type));
    }

    /** 특정 중심 단어의 모든 클러스터 조회 (모든 사용자가 공유) */
    public List<ClusterWord> getCluster(Long centerWordId) {
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("기준 단어를 찾을 수 없습니다."));

        return clusterWordRepository.findByCenterWord(centerWord);
    }

    /** 특정 중심 단어의 클러스터 조회 (모든 사용자가 공유) */
    public List<ClusterWord> getMyClustersByCenter(Long centerWordId) {
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("기준 단어를 찾을 수 없습니다."));
        return clusterWordRepository.findByCenterWord(centerWord);
    }

    /** 특정 중심 단어의 클러스터 전체 삭제 (관리자 전용) */
    @Transactional
    public void deleteCluster(Long centerWordId) {
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("기준 단어를 찾을 수 없습니다."));
        clusterWordRepository.deleteByCenterWord(centerWord);
    }



    /**
     * DeepSeek API를 이용해서 단어의 유의어와 반의어 조회
     */
    private WordRelations getWordRelationsFromDeepSeek(String word) throws Exception {
        if (deepseekApiKey == null || deepseekApiKey.isEmpty()) {
            throw new RuntimeException("DeepSeek API 키가 설정되지 않았습니다.");
        }

        String prompt = String.format(
            "For the English word '%s', provide:\n" +
            "1. 3-5 synonyms (words with similar meaning)\n" +
            "2. 3-5 antonyms (words with opposite meaning)\n\n" +
            "Respond in JSON format:\n" +
            "{\n" +
            "  \"synonyms\": [\"word1\", \"word2\", ...],\n" +
            "  \"antonyms\": [\"word1\", \"word2\", ...]\n" +
            "}\n\n" +
            "Only return the JSON, no other text.",
            word
        );

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .build();

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        JSONArray messages = new JSONArray();
        messages.put(userMessage);

        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("model", "deepseek-chat");
        requestBodyJson.put("messages", messages);
        requestBodyJson.put("temperature", 0.3);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                requestBodyJson.toString()
        );

        Request request = new Request.Builder()
                .url(DEEPSEEK_URL)
                .addHeader("Authorization", "Bearer " + deepseekApiKey)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        
        if (!response.isSuccessful()) {
            String errorBody = response.body() != null ? response.body().string() : "No error body";
            System.err.println("DeepSeek API 호출 실패: HTTP " + response.code() + " - " + errorBody);
            throw new RuntimeException("DeepSeek API 호출 실패: HTTP " + response.code() + " - " + errorBody);
        }

        String responseJson = response.body().string();

        JSONObject jsonObj = new JSONObject(responseJson);
        
        if (!jsonObj.has("choices") || jsonObj.getJSONArray("choices").length() == 0) {
            System.err.println("DeepSeek API 응답에 choices가 없습니다: " + responseJson);
            throw new RuntimeException("DeepSeek API 응답에 choices가 없습니다: " + responseJson);
        }

        String content = jsonObj
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        // JSON 파싱 (응답에서 JSON 부분만 추출)
        String jsonContent = extractJson(content);
        
        JSONObject relationsJson = new JSONObject(jsonContent);

        List<String> synonyms = new ArrayList<>();
        List<String> antonyms = new ArrayList<>();

        if (relationsJson.has("synonyms")) {
            JSONArray synonymsArray = relationsJson.getJSONArray("synonyms");
            for (int i = 0; i < synonymsArray.length(); i++) {
                synonyms.add(synonymsArray.getString(i).toLowerCase().trim());
            }
        }

        if (relationsJson.has("antonyms")) {
            JSONArray antonymsArray = relationsJson.getJSONArray("antonyms");
            for (int i = 0; i < antonymsArray.length(); i++) {
                antonyms.add(antonymsArray.getString(i).toLowerCase().trim());
            }
        }

        return new WordRelations(synonyms, antonyms);
    }

    /**
     * 응답 텍스트에서 JSON 부분만 추출
     */
    private String extractJson(String text) {
        // JSON 객체 시작/끝 찾기
        int startIdx = text.indexOf("{");
        int endIdx = text.lastIndexOf("}");
        
        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            return text.substring(startIdx, endIdx + 1);
        }
        
        return text;
    }

    /**
     * 유의어/반의어 결과를 담는 내부 클래스
     */
    private static class WordRelations {
        private final List<String> synonyms;
        private final List<String> antonyms;

        public WordRelations(List<String> synonyms, List<String> antonyms) {
            this.synonyms = synonyms;
            this.antonyms = antonyms;
        }

        public List<String> getSynonyms() {
            return synonyms;
        }

        public List<String> getAntonyms() {
            return antonyms;
        }
    }
}
