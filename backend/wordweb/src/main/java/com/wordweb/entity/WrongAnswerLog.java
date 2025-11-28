package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "WRONG_ANSWER_LOG")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WrongAnswerLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wrong_log_seq")
    @SequenceGenerator(name = "wrong_log_seq", sequenceName = "SEQ_WRONG_WORD_ID", allocationSize = 1)
    @Column(name = "WRONG_WORD_ID")
    private Long wrongWordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORD_ID", nullable = false)
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "WRONG_AT", nullable = false)
    private LocalDateTime wrongAt;

    @Column(name = "IS_USED_IN_STORY")
    private Boolean isUsedInStory;  // boolean이 훨씬 직관적

    /** 최초 생성 (틀린 단어 저장) */
    /** 최초 생성 (틀린 단어 저장) */
    public static WrongAnswerLog create(User user, Word word) {
        return WrongAnswerLog.builder()
                .user(user)
                .word(word)
                .wrongAt(LocalDateTime.now())
                .isUsedInStory(false)
                .build();
    }


    /** 스토리 생성에 사용되었을 때 Y 처리 */
    public void markUsedInStory() {
        this.isUsedInStory = true;
    }
}
