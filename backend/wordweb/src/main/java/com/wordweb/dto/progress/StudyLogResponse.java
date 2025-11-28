package com.wordweb.dto.progress;

import com.wordweb.entity.StudyLog;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyLogResponse {

    private Long wordId;
    private String status; // CORRECT / WRONG / NONE

    public static StudyLogResponse from(StudyLog log) {
        return StudyLogResponse.builder()
                .wordId(log.getWord().getWordId())
                .status(log.getStatus())
                .build();
    }
}
