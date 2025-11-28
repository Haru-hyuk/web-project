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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "study_log_seq")
    @SequenceGenerator(name = "study_log_seq", sequenceName = "SEQ_STUDY_LOG_ID", allocationSize = 1)
    @Column(name = "STUDY_LOG_ID")
    private Long studyLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORD_ID", nullable = false)
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    /** 학습 상태: learned / pending */
    @Column(name = "STATUS", nullable = false)
    private String status;

    /** 마지막 문제 정답 여부: correct / wrong */
    @Column(name = "LAST_RESULT")
    private String lastResult;

    @Column(name = "LAST_STUDY_AT")
    private LocalDateTime lastStudyAt;

    @Column(name = "TOTAL_CORRECT")
    private Integer totalCorrect;

    @Column(name = "TOTAL_WRONG")
    private Integer totalWrong;

    /** 최초 생성 */
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

    /** 정답 처리 */
    public void markCorrect() {
        this.lastResult = "correct";
        this.lastStudyAt = LocalDateTime.now();
        this.totalCorrect += 1;
        this.status = "learned";
    }

    /** 오답 처리 */
    public void markWrong() {
        this.lastResult = "wrong";
        this.lastStudyAt = LocalDateTime.now();
        this.totalWrong += 1;
        this.status = "pending";
    }
}
