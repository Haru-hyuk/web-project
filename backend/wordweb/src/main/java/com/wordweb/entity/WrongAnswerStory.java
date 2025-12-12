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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STORY_ID")
    private Long storyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    /** 영어 제목 */
    @Column(name = "TITLE", nullable = false)
    private String title;

    /** ⭐ 한글 제목 */
    @Column(name = "TITLE_KO")
    private String titleKo;

    @Lob
    @Column(name = "STORY_EN", nullable = false)
    private String storyEn;

    @Lob
    @Column(name = "STORY_KO", nullable = false)
    private String storyKo;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    /** ✅ 스토리 생성 팩토리 (최종) */
    public static WrongAnswerStory create(
            User user,
            String title,
            String titleKo,
            String storyEn,
            String storyKo
    ) {
        return WrongAnswerStory.builder()
                .user(user)
                .title(title)
                .titleKo(titleKo)
                .storyEn(storyEn)
                .storyKo(storyKo)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /** (선택) 영어 제목 수정 */
    public void updateTitle(String newTitle) {
        this.title = newTitle;
    }

    /** (선택) 한글 제목 수정 */
    public void updateTitleKo(String newTitleKo) {
        this.titleKo = newTitleKo;
    }
}
