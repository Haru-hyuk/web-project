package com.wordweb.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 단어 임베딩 벡터 처리 및 유사도 계산 서비스
 */
@Service
@Slf4j
public class EmbeddingService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * JSON 문자열을 double 배열로 파싱
     */
    public double[] parseEmbedding(String embeddingJson) {
        if (embeddingJson == null || embeddingJson.trim().isEmpty()) {
            return new double[0];
        }

        try {
            List<Double> list = objectMapper.readValue(embeddingJson, new TypeReference<List<Double>>() {});
            return list.stream().mapToDouble(Double::doubleValue).toArray();
        } catch (Exception e) {
            log.error("임베딩 파싱 오류: {}", e.getMessage());
            return new double[0];
        }
    }

    /**
     * double 배열을 JSON 문자열로 변환
     */
    public String embeddingToJson(double[] embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (Exception e) {
            log.error("임베딩 JSON 변환 오류: {}", e.getMessage());
            return "[]";
        }
    }

    /**
     * 코사인 유사도 계산
     *
     * @param embedding1 첫 번째 임베딩 벡터
     * @param embedding2 두 번째 임베딩 벡터
     * @return 코사인 유사도 (0~1 범위, 1에 가까울수록 유사)
     */
    public double cosineSimilarity(double[] embedding1, double[] embedding2) {
        if (embedding1.length == 0 || embedding2.length == 0) {
            return 0.0;
        }

        if (embedding1.length != embedding2.length) {
            log.warn("임베딩 벡터 길이가 다릅니다: {} vs {}", embedding1.length, embedding2.length);
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            norm1 += embedding1[i] * embedding1[i];
            norm2 += embedding2[i] * embedding2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 코사인 유사도 계산 (JSON 문자열 입력)
     */
    public double cosineSimilarity(String embeddingJson1, String embeddingJson2) {
        double[] embedding1 = parseEmbedding(embeddingJson1);
        double[] embedding2 = parseEmbedding(embeddingJson2);
        return cosineSimilarity(embedding1, embedding2);
    }

    /**
     * 유클리드 거리 계산
     */
    public double euclideanDistance(double[] embedding1, double[] embedding2) {
        if (embedding1.length == 0 || embedding2.length == 0 || embedding1.length != embedding2.length) {
            return Double.MAX_VALUE;
        }

        double sum = 0.0;
        for (int i = 0; i < embedding1.length; i++) {
            double diff = embedding1[i] - embedding2[i];
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    /**
     * 벡터 정규화 (L2 Normalization)
     */
    public double[] normalizeVector(double[] embedding) {
        double norm = 0.0;
        for (double v : embedding) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);

        if (norm == 0.0) {
            return embedding;
        }

        double[] normalized = new double[embedding.length];
        for (int i = 0; i < embedding.length; i++) {
            normalized[i] = embedding[i] / norm;
        }

        return normalized;
    }
}
