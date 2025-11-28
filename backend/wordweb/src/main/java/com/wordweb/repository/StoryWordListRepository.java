package com.wordweb.repository;

import com.wordweb.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryWordListRepository extends JpaRepository<StoryWordList, StoryWordListId> {

    /** 스토리에 연결된 모든 단어 조회 */
    List<StoryWordList> findByStory(WrongAnswerStory story);

    /** 특정 WrongAnswerLog로 연결된 스토리 조회 */
    List<StoryWordList> findByWrongAnswerLog(WrongAnswerLog log);

    /** storyId 기반 조회 (필요 시) */
    List<StoryWordList> findByStoryId(Long storyId);

    /** wrongWordId 기반 조회 (필요 시) */
    List<StoryWordList> findByWrongWordId(Long wrongWordId);

    /** 특정 스토리에 특정 단어가 이미 포함되어 있는지 검사 */
    boolean existsByStoryAndWrongAnswerLog(WrongAnswerStory story, WrongAnswerLog log);
}
