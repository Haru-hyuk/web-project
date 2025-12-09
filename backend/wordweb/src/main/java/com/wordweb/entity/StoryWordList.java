package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "STORY_WORD_LIST")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@IdClass(StoryWordListId.class)
public class StoryWordList {

    @Id
    @Column(name = "STORY_ID")
    private Long storyId;

    @Id
    @Column(name = "WORD_ID")
    private Long wordId;  // 단어 ID (복합키 - 히스토리 보존용)

    @Column(name = "WRONG_WORD_ID", nullable = true)
    private Long wrongWordId;  // 오답 기록 ID (선택적, NULL 허용)

    /** Story 연결 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORY_ID", insertable = false, updatable = false)
    private WrongAnswerStory story;

    /** WrongAnswerLog 연결 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WRONG_WORD_ID", insertable = false, updatable = false)
    private WrongAnswerLog wrongAnswerLog;

    /** Word 연결 (히스토리 보존용 - WRONG_ANSWER_LOG 삭제되어도 조회 가능) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORD_ID", insertable = false, updatable = false)
    private Word word;

    /** 생성 메서드 - wordId(필수) + wrongWordId(선택적) 저장 */
    public static StoryWordList create(Long storyId, Long wordId, Long wrongWordId) {
        StoryWordList list = new StoryWordList();
        list.storyId = storyId;
        list.wordId = wordId;  // 복합키 일부 (필수)
        list.wrongWordId = wrongWordId;  // 선택적 (NULL 허용)
        return list;
    }
}
