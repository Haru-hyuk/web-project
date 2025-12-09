package com.wordweb.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "STORY_WORD_LIST")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(StoryWordListId.class)
public class StoryWordList {

    @Id
    @Column(name = "STORY_ID")
    private Long storyId;

    @Id
    @Column(name = "WORD_ID")
    private Long wordId;

    @Column(name = "WRONG_WORD_ID")
    private Long wrongWordId;

    /** 🔥 순환 참조 방지 포인트 */
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORY_ID", insertable = false, updatable = false)
    private WrongAnswerStory story;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORD_ID", insertable = false, updatable = false)
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WRONG_WORD_ID", insertable = false, updatable = false)
    private WrongAnswerLog wrongAnswerLog;

    public static StoryWordList create(Long storyId, Long wordId, Long wrongWordId) {
        StoryWordList list = new StoryWordList();
        list.storyId = storyId;
        list.wordId = wordId;
        list.wrongWordId = wrongWordId;
        return list;
    }
}
