package com.wordweb.dto.progress;

import com.wordweb.entity.StudyLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StudyLogResponse {

    private Long wordId;
    private String status; // learned / pending / NONE
    private Integer totalCorrect;
    private Integer totalWrong;
    private LocalDateTime lastStudyAt;

    public static StudyLogResponse from(StudyLog log) {
        return StudyLogResponse.builder()
                .wordId(log.getWord().getWordId())
                .status(log.getStatus())
                .totalCorrect(log.getTotalCorrect())
                .totalWrong(log.getTotalWrong())
                .lastStudyAt(log.getLastStudyAt())
                .build();
    }
    
    /** StudyLog가 없는 경우 (학습 기록 없음) */
    public static StudyLogResponse empty(Long wordId) {
        return StudyLogResponse.builder()
                .wordId(wordId)
                .status("NONE")
                .totalCorrect(0)
                .totalWrong(0)
                .lastStudyAt(null)
                .build();
    }
}
