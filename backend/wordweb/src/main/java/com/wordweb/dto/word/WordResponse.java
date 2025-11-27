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
    private Integer level;

    private boolean isFavorite;     // 즐겨찾기 여부
    private String learningStatus;  // NONE / IN_PROGRESS / DONE

    public static WordResponse from(Word word, boolean isFavorite, String learningStatus) {
        return WordResponse.builder()
                .wordId(word.getWordId())
                .word(word.getWord())
                .meaning(word.getMeaning())
                .partOfSpeech(word.getPartOfSpeech())
                .exampleSentence(word.getExampleSentenceEn())
                .category(word.getCategory())
                .level(word.getLevel())
                .isFavorite(isFavorite)
                .learningStatus(learningStatus)
                .build();
    }
}
