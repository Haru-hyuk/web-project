package com.wordweb.dto.learn;

import lombok.Getter;

import java.util.List;

@Getter
public class QuizResultRequest {

    private List<Answer> answers;

    @Getter
    public static class Answer {
        private Long wordId;
        private boolean correct;
    }
}
