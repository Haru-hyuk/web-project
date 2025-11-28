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

    private String exampleSentenceEn;   // 영어 예문
    private String exampleSentenceKo;   // 한국어 예문

    private String category;
    private Integer level;              // 엔티티의 LEVEL 필드와 일치

    private boolean favorite;
    private String learningStatus;

    public static WordResponse from(Word word, boolean favorite, String learningStatus) {
        return WordResponse.builder()
                .wordId(word.getWordId())
                .word(word.getWord())
                .meaning(word.getMeaning())
                .partOfSpeech(word.getPartOfSpeech())
                .exampleSentenceEn(word.getExampleSentenceEn())
                .exampleSentenceKo(word.getExampleSentenceKo())
                .category(word.getCategory())
                .level(word.getLevel())  // ✔ level로 변경
                .favorite(favorite)
                .learningStatus(learningStatus)
                .build();
    }
}
