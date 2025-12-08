package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "WRONG_ANSWER_LOG")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WrongAnswerLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Boolean isUsedInStory;


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
