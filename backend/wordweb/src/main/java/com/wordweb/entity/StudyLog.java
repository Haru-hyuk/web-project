package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "STUDY_LOG")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STUDY_LOG_ID")
    private Long studyLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORD_ID", nullable = false)
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "STATUS", nullable = false)
    private String status;

    @Column(name = "LAST_RESULT")
    private String lastResult;

    @Column(name = "LAST_STUDY_AT")
    private LocalDateTime lastStudyAt;

    @Column(name = "TOTAL_CORRECT")
    private Integer totalCorrect;

    @Column(name = "TOTAL_WRONG")
    private Integer totalWrong;

    public static StudyLog create(User user, Word word) {
        return StudyLog.builder()
                .user(user)
                .word(word)
                .status("pending")
                .lastResult(null)
                .lastStudyAt(LocalDateTime.now())
                .totalCorrect(0)
                .totalWrong(0)
                .build();
    }

    public void markCorrect() {
        this.lastResult = "correct";
        this.lastStudyAt = LocalDateTime.now();
        this.totalCorrect += 1;
        this.status = "learned";
    }

    public void markWrong() {
        this.lastResult = "wrong";
        this.lastStudyAt = LocalDateTime.now();
        this.totalWrong += 1;
        this.status = "pending";
    }
}
