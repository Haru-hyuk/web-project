package com.wordweb.dto.word;

import com.wordweb.entity.Word;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WordResponse {

    private Long wordId;
    private String word;
    private String meaning;
    private String partOfSpeech;
    private String exampleSentence;
    private String category;
    private Integer wordLevel; // ★ 변경됨

    private boolean isFavorite;
    private String learningStatus;

    public static WordResponse from(Word word, boolean isFavorite, String learningStatus) {
        return WordResponse.builder()
                .wordId(word.getWordId())
                .word(word.getWord())
                .meaning(word.getMeaning())
                .partOfSpeech(word.getPartOfSpeech())
                .exampleSentence(word.getExampleSentence())
                .category(word.getCategory())
                .wordLevel(word.getWordLevel())  // ★ 수정됨
                .isFavorite(isFavorite)
                .learningStatus(learningStatus)
                .build();
    }
}
