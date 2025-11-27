package com.wordweb.service;

import com.wordweb.entity.ClusterWord;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.repository.ClusterWordRepository;
import com.wordweb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 간단한 단어 클러스터링 서비스
 * 기존 ClusterWord 엔티티 활용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterService {

    private final ClusterWordRepository clusterWordRepository;
    private final WordRepository wordRepository;
    private final EmbeddingService embeddingService;

    /**
     * 특정 단어와 유사한 단어 찾기
     */
    @Transactional
    public List<ClusterWord> createCluster(User user, Long centerWordId, int topN, double threshold) {
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다"));

        if (centerWord.getEmbedding() == null) {
            log.warn("단어에 임베딩이 없습니다: {}", centerWord.getWord());
            return new ArrayList<>();
        }

        double[] centerEmbedding = embeddingService.parseEmbedding(centerWord.getEmbedding());
        List<ClusterWord> clusters = new ArrayList<>();

        // 모든 단어와 유사도 계산
        wordRepository.findAll().forEach(word -> {
            if (word.getWordId().equals(centerWordId) || word.getEmbedding() == null) {
                return;
            }

            double[] embedding = embeddingService.parseEmbedding(word.getEmbedding());
            double similarity = embeddingService.cosineSimilarity(centerEmbedding, embedding);

            if (similarity >= threshold) {
                ClusterWord cluster = ClusterWord.builder()
                        .user(user)
                        .centerWord(centerWord)
                        .relatedWord(word)
                        .score(similarity)
                        .relationType("similarity")
                        .createdAt(new Timestamp(System.currentTimeMillis()))
                        .build();
                clusters.add(cluster);
            }
        });

        // 유사도 순으로 정렬
        clusters.sort(Comparator.comparingDouble(ClusterWord::getScore).reversed());

        // 상위 N개만 저장
        List<ClusterWord> topClusters = clusters.subList(0, Math.min(topN, clusters.size()));
        return clusterWordRepository.saveAll(topClusters);
    }

    /**
     * 사용자의 특정 단어에 대한 클러스터 조회
     */
    public List<ClusterWord> getClusters(User user, Word centerWord) {
        return clusterWordRepository.findByUserAndCenterWord(user, centerWord);
    }

    /**
     * 사용자의 모든 클러스터 조회
     */
    public List<ClusterWord> getAllClusters(User user) {
        return clusterWordRepository.findByUser(user);
    }
}
