package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "WRONG_ANSWER_STORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WrongAnswerStory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "story_seq")
    @SequenceGenerator(name = "story_seq", sequenceName = "SEQ_STORY_ID", allocationSize = 1)
    @Column(name = "STORY_ID")
    private Long storyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Lob
    @Column(name = "TITLE")
    private String title;

    @Lob
    @Column(name = "STORY_EN")
    private String storyEn;

    @Lob
    @Column(name = "STORY_KO")
    private String storyKo;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** 스토리 생성 팩토리 */
    public static WrongAnswerStory create(User user, String title, String storyEn, String storyKo) {
        return WrongAnswerStory.builder()
                .user(user)
                .title(title)
                .storyEn(storyEn)
                .storyKo(storyKo)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /** 제목을 나중에 수정해야 한다면(선택) */
    public void updateTitle(String newTitle) {
        this.title = newTitle;
    }
}
