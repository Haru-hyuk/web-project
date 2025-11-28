package com.wordweb.dto.favorite;

import com.wordweb.entity.FavoriteWord;
import com.wordweb.entity.Word;

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

    private String exampleSentenceEn;
    private String exampleSentenceKo;

    private String category;
    private Integer level;

    public static FavoriteWordResponse from(FavoriteWord fw) {
        Word w = fw.getWord();

        return FavoriteWordResponse.builder()
                .id(fw.getFavoriteWordId())
                .wordId(w.getWordId())
                .word(w.getWord())
                .meaning(w.getMeaning())
                .partOfSpeech(w.getPartOfSpeech())
                .exampleSentenceEn(w.getExampleSentenceEn())
                .exampleSentenceKo(w.getExampleSentenceKo())
                .category(w.getCategory())
                .level(w.getLevel())
                .build();
    }
}
