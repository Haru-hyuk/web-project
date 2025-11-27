package com.wordweb.dto.favorite;

import com.wordweb.entity.FavoriteWord;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FavoriteWordResponse {

    private Long id;
    private Long wordId;
    private String word;
    private String meaning;
    private String partOfSpeech;
    private String exampleSentence;
    private String category;
    private Integer wordLevel;   // ★ 변경됨

    public static FavoriteWordResponse from(FavoriteWord fw) {
        return FavoriteWordResponse.builder()
                .id(fw.getFavoriteWordId())
                .wordId(fw.getWord().getWordId())
                .word(fw.getWord().getWord())
                .meaning(fw.getWord().getMeaning())
                .partOfSpeech(fw.getWord().getPartOfSpeech())
                .exampleSentence(fw.getWord().getExampleSentence())
                .category(fw.getWord().getCategory())
                .wordLevel(fw.getWord().getWordLevel()) // ★ 수정됨
                .build();
    }
}
