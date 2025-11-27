package com.wordweb.dto.clustering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 단어 클러스터 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordClusterResponse {

    /**
     * 중심 단어 정보
     */
    private CenterWord centerWord;

    /**
     * 연관 단어 목록
     */
    private List<RelatedWordResponse> relatedWords;

    /**
     * 중심 단어 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CenterWord {
        private Long wordId;
        private String word;
        private String meaning;
        private String partOfSpeech;
        private String category;
        private Integer level;
    }
}
