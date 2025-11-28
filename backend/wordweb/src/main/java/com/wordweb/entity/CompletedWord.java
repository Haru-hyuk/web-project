package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "COMPLETED_WORD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CompletedWord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "completed_seq")
    @SequenceGenerator(name = "completed_seq", sequenceName = "SEQ_COMPLETED_WORD_ID", allocationSize = 1)
    @Column(name = "COMPLETED_WORD_ID")
    private Long completedWordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORD_ID", nullable = false)
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

    /** 학습 완료 엔티티 생성 */
    public static CompletedWord create(User user, Word word) {
        return CompletedWord.builder()
                .user(user)
                .word(word)
                .completedAt(LocalDateTime.now())
                .build();
    }
}
