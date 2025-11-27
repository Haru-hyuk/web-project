package com.wordweb.dto.clustering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 연관 단어 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatedWordResponse {

    /**
     * 연관 단어 ID
     */
    private Long wordId;

    /**
     * 연관 단어 텍스트
     */
    private String word;

    /**
     * 단어 뜻
     */
    private String meaning;

    /**
     * 유사도 점수 (0.0 ~ 1.0)
     */
    private Double score;

    /**
     * 관계 유형
     */
    private String relationType;

    /**
     * 품사
     */
    private String partOfSpeech;

    /**
     * 카테고리
     */
    private String category;

    /**
     * 난이도
     */
    private Integer level;
}
