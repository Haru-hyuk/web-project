package com.wordweb.dto.clustering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 클러스터링 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClusteringRequest {

    /**
     * 학습한 단어 ID 목록
     */
    private List<Long> wordIds;

    /**
     * 각 단어마다 찾을 연관 단어 개수
     */
    @Builder.Default
    private Integer topN = 10;

    /**
     * 최소 유사도 임계값 (0.0 ~ 1.0)
     */
    @Builder.Default
    private Double similarityThreshold = 0.5;
}
