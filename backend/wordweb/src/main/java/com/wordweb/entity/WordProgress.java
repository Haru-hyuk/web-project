package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Table(name = "WORD_PROGRESS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROGRESS_ID")
    private Long progressId;

    // 유저 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    // 단어 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORD_ID", nullable = false)
    private Word word;

    @Column(name = "STATUS", nullable = false)
    private String status;   // IN_PROGRESS / DONE

    @Column(name = "UPDATED_AT", nullable = false)
    private Timestamp updatedAt;

    // 🔥 대시보드용: 학습 날짜
    @Column(name = "STUDY_DATE", nullable = false)
    private LocalDate studyDate;

    // 자동으로 날짜 넣기 위한 헬퍼
    @PrePersist
    public void onCreate() {
        if (this.studyDate == null) {
            this.studyDate = LocalDate.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = new Timestamp(System.currentTimeMillis());
        }
    }
}
