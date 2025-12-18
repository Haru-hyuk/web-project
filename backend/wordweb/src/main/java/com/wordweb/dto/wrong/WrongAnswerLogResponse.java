package com.wordweb.dto.wrong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WrongAnswerLogResponse {
    private Long wrongWordId;
    private Long wordId;
    private String word;
    private String meaning;
    private Integer level;
    private LocalDateTime wrongAt;
    private Boolean isUsedInStory;
    private Integer totalWrong; // StudyLog의 totalWrong 값
}


