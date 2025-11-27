package com.wordweb.dto.clustering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 유사 단어 검색 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimilarWordsResponse {

    /**
     * 검색 기준 단어 ID
     */
    private Long searchWordId;

    /**
     * 검색 기준 단어
     */
    private String searchWord;

    /**
     * 유사한 단어 목록
     */
    private List<RelatedWordResponse> similarWords;

    /**
     * 총 개수
     */
    private Integer totalCount;
}
