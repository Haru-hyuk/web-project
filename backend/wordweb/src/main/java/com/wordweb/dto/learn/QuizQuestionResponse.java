package com.wordweb.dto.learn;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuizQuestionResponse {
    private Long wordId;
    private String word;
    private List<String> options;
    private int answerIndex;
}
