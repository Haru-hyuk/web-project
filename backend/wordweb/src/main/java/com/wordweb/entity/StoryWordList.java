package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "STORY_WORD_LIST")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@IdClass(StoryWordListId.class)
public class StoryWordList {

    @Id
    @Column(name = "STORY_ID")
    private Long storyId;

    @Id
    @Column(name = "WRONG_WORD_ID")
    private Long wrongWordId;

    /** Story 연결 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORY_ID", insertable = false, updatable = false)
    private WrongAnswerStory story;

    /** WrongAnswerLog 연결 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WRONG_WORD_ID", insertable = false, updatable = false)
    private WrongAnswerLog wrongAnswerLog;

    /** 생성 메서드 */
    public static StoryWordList create(Long storyId, Long wrongWordId) {
        StoryWordList list = new StoryWordList();
        list.storyId = storyId;
        list.wrongWordId = wrongWordId;
        return list;
    }
}
