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
    @Column(name = "WORD_ID")
    private Long wordId;

    @Column(name = "WRONG_WORD_ID")
    private Long wrongWordId;  // NULL 가능 (optional link)

    /** Story 연결 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORY_ID", insertable = false, updatable = false)
    private WrongAnswerStory story;

    /** Word 연결 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORD_ID", insertable = false, updatable = false)
    private Word word;

    /** WrongAnswerLog 연결 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WRONG_WORD_ID", insertable = false, updatable = false)
    private WrongAnswerLog wrongAnswerLog;

    /** 생성 메서드 */
    public static StoryWordList create(Long storyId, Long wordId, Long wrongWordId) {
        StoryWordList list = new StoryWordList();
        list.storyId = storyId;
        list.wordId = wordId;
        list.wrongWordId = wrongWordId;
        return list;
    }
}
