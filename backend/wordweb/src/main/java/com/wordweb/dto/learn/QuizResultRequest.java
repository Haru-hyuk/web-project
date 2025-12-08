package com.wordweb.dto.learn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultRequest {

    @JsonProperty("mode")
    private String mode; // "normal" | "wrong" - 퀴즈 모드
    
    @JsonProperty("answers")
    private List<Answer> answers;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        @JsonProperty("wordId")
        private Long wordId;
        
        @JsonProperty("correct")
        private boolean correct;  // boolean (원시 타입) - null 허용 안 함
    }
}
