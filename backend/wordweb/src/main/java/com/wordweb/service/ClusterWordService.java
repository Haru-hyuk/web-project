package com.wordweb.service;

import com.wordweb.entity.ClusterWord;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.repository.ClusterWordRepository;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.security.SecurityUtil;
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
    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final EmbeddingService embeddingService;

    /** 로그인 유저 가져오기 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저 정보를 찾을 수 없습니다."));
    }

    /**
     * 특정 단어와 유사한 단어 찾기 (유사도 기반)
     */
    @Transactional
    public List<ClusterWord> createCluster(Long centerWordId) {
        User user = getLoginUser();
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다"));

        List<ClusterWord> clusters = new ArrayList<>();
        Set<Long> addedWordIds = new HashSet<>();
        addedWordIds.add(centerWordId); // 중심 단어는 제외

        // DeepSeek API로 중심 단어의 유의어/반의어 찾기 (우선 처리)
        int synonymCount = 0;
        int antonymCount = 0;
        try {
            System.out.println("중심 단어 '" + centerWord.getWord() + "'에 대한 유의어/반의어 조회 시작");
            WordRelations relations = getWordRelationsFromDeepSeek(centerWord.getWord());
            System.out.println("DeepSeek API 응답: 유의어 " + relations.getSynonyms().size() + "개, 반의어 " + relations.getAntonyms().size() + "개");

            // 유의어 추가 (반드시 포함되도록 우선 처리, 최대 10개 제한)
            for (String synonym : relations.getSynonyms()) {
                if (clusters.size() >= DEFAULT_TOP_N) break; // 최대 10개 제한
                
                Word synonymWord = wordRepository.findByWord(synonym).orElse(null);
                if (synonymWord != null && !addedWordIds.contains(synonymWord.getWordId())) {
                    ClusterWord cluster = ClusterWord.create(user, centerWord, synonymWord, 0.9, "synonym");
                    if (!clusterWordRepository.existsByUserAndCenterWordAndRelatedWord(user, centerWord, synonymWord)) {
                        clusters.add(cluster);
                        addedWordIds.add(synonymWord.getWordId());
                        synonymCount++;
                        System.out.println("유의어 추가: " + synonym);
                    }
                } else if (synonymWord == null) {
                    System.out.println("유의어 '" + synonym + "'는 DB에 없어서 추가되지 않음");
                }
            }

            // 반의어 추가 (반드시 포함되도록 우선 처리, 최대 10개 제한)
            for (String antonym : relations.getAntonyms()) {
                if (clusters.size() >= DEFAULT_TOP_N) break; // 최대 10개 제한
                
                Word antonymWord = wordRepository.findByWord(antonym).orElse(null);
                if (antonymWord != null && !addedWordIds.contains(antonymWord.getWordId())) {
                    ClusterWord cluster = ClusterWord.create(user, centerWord, antonymWord, 0.7, "antonym");
                    if (!clusterWordRepository.existsByUserAndCenterWordAndRelatedWord(user, centerWord, antonymWord)) {
                        clusters.add(cluster);
                        addedWordIds.add(antonymWord.getWordId());
                        antonymCount++;
                        System.out.println("반의어 추가: " + antonym);
                    }
                } else if (antonymWord == null) {
                    System.out.println("반의어 '" + antonym + "'는 DB에 없어서 추가되지 않음");
                }
            }

            // 유의어/반의어가 하나도 추가되지 않은 경우 경고
            if (synonymCount == 0 && antonymCount == 0) {
                System.err.println("경고: 중심 단어 '" + centerWord.getWord() + "'에 대한 유의어/반의어가 DB에 없어서 추가되지 않았습니다.");
            } else {
                System.out.println("유의어 " + synonymCount + "개, 반의어 " + antonymCount + "개 추가 완료");
            }
        } catch (Exception e) {
            // API 호출 실패 시 경고
            System.err.println("중심 단어 '" + centerWord.getWord() + "'의 유의어/반의어 조회 실패: " + e.getMessage());
            e.printStackTrace();
        }

        // 유사도 기반 단어 추가 (임베딩이 있는 경우에만)
        if (centerWord.getEmbedding() != null) {
            double[] centerEmbedding = embeddingService.parseEmbedding(centerWord.getEmbedding());
            List<ClusterWord> similarityClusters = new ArrayList<>();

            // 모든 단어와 유사도 계산
            wordRepository.findAll().forEach(word -> {
                if (addedWordIds.contains(word.getWordId()) || word.getEmbedding() == null) {
                    return;
                }

                double[] embedding = embeddingService.parseEmbedding(word.getEmbedding());
                double similarity = embeddingService.cosineSimilarity(centerEmbedding, embedding);

                if (similarity >= DEFAULT_THRESHOLD) {
                    ClusterWord cluster = ClusterWord.create(user, centerWord, word, similarity, "similarity");
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
                System.out.println("유사도 기반 단어 " + limit + "개 추가 완료");
            } else {
                System.out.println("유사도 기반 단어 추가 생략 (이미 최대 개수 도달)");
            }
        }

        // 모든 클러스터 저장
        return clusterWordRepository.saveAll(clusters);
    }


    /** 특정 중심 단어에 대한 클러스터 추가 */
    public void addCluster(Long centerWordId, Long relatedWordId, Double score, String type) {
        User user = getLoginUser();

        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("기준 단어를 찾을 수 없습니다."));

        Word relatedWord = wordRepository.findById(relatedWordId)
                .orElseThrow(() -> new RuntimeException("연관 단어를 찾을 수 없습니다."));

        // 중복 방지
        if (clusterWordRepository.existsByUserAndCenterWordAndRelatedWord(user, centerWord, relatedWord)) {
            return; // 이미 존재하면 아무것도 안 함 (idempotent)
        }

        clusterWordRepository.save(ClusterWord.create(user, centerWord, relatedWord, score, type));
    }

    /** 특정 중심 단어의 모든 클러스터 조회 */
    public List<ClusterWord> getCluster(Long centerWordId) {
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("기준 단어를 찾을 수 없습니다."));

        return clusterWordRepository.findByCenterWord(centerWord);
    }

    /** 유저가 가진 전체 클러스터 조회 */
    public List<ClusterWord> getMyClusters() {
        User user = getLoginUser();
        return clusterWordRepository.findByUser(user);
    }

    /** 특정 중심 단어의 클러스터를 유저 기준으로 조회 */
    public List<ClusterWord> getMyClustersByCenter(Long centerWordId) {
        User user = getLoginUser();
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("기준 단어를 찾을 수 없습니다."));
        return clusterWordRepository.findByUserAndCenterWord(user, centerWord);
    }

    /** 특정 중심 단어의 클러스터 전체 삭제 (사용자 기준) */
    @Transactional
    public void deleteCluster(Long centerWordId) {
        User user = getLoginUser();
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("기준 단어를 찾을 수 없습니다."));
        clusterWordRepository.deleteByUserAndCenterWord(user, centerWord);
    }

    /** 사용자의 모든 클러스터 삭제 */
    @Transactional
    public void deleteAllClusters() {
        User user = getLoginUser();
        clusterWordRepository.deleteByUser(user);
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
            throw new RuntimeException("DeepSeek API 호출 실패: HTTP " + response.code() + " - " + errorBody);
        }

        String responseJson = response.body().string();
        System.out.println("DeepSeek API 원본 응답: " + responseJson);

        JSONObject jsonObj = new JSONObject(responseJson);
        
        if (!jsonObj.has("choices") || jsonObj.getJSONArray("choices").length() == 0) {
            throw new RuntimeException("DeepSeek API 응답에 choices가 없습니다: " + responseJson);
        }

        String content = jsonObj
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        System.out.println("DeepSeek API content: " + content);

        // JSON 파싱 (응답에서 JSON 부분만 추출)
        String jsonContent = extractJson(content);
        System.out.println("추출된 JSON: " + jsonContent);
        
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
