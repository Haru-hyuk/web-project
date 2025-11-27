package com.wordweb.dto.favorite;

import com.wordweb.entity.FavoriteWord;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FavoriteWordResponse {

    private Long id;             // 즐겨찾기 PK
    private Long wordId;         // 단어 PK
    private String word;         // 단어
    private String meaning;      // 뜻
    private String partOfSpeech; // 품사
    private String exampleSentence; // 예문
    private String category;     // 카테고리
    private Integer level;      // 레벨

    public static FavoriteWordResponse from(FavoriteWord fw) {
        return FavoriteWordResponse.builder()
                .id(fw.getFavoriteWordId())
                .wordId(fw.getWord().getWordId())
                .word(fw.getWord().getWord())
                .meaning(fw.getWord().getMeaning())
                .partOfSpeech(fw.getWord().getPartOfSpeech())
                .exampleSentence(fw.getWord().getExampleSentenceEn())
                .category(fw.getWord().getCategory())
                .level(fw.getWord().getLevel())
                .build();
    }
    
    
}
