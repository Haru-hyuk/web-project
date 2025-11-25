package com.wordweb.dto.word;

import com.wordweb.entity.Word;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WordResponse {

    private Long wordId;
    private String word;
    private String meaning;
    private String partOfSpeech;
    private String exampleSentence;
    private String category;
    private String level;

    public static WordResponse from(Word w) {
        return WordResponse.builder()
                .wordId(w.getWordId())
                .word(w.getWord())
                .meaning(w.getMeaning())
                .partOfSpeech(w.getPartOfSpeech())
                .exampleSentence(w.getExampleSentence())
                .category(w.getCategory())
                .level(w.getLevel())
                .build();
    }
}
