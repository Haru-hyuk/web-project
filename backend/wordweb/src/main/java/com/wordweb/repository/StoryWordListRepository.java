package com.wordweb.repository;

import com.wordweb.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoryWordListRepository extends JpaRepository<StoryWordList, StoryWordListId> {

    /** 스토리에 연결된 모든 단어 조회 */
    List<StoryWordList> findByStory(WrongAnswerStory story);

    /** storyId 기반 조회 */
    List<StoryWordList> findByStoryId(Long storyId);

    /** storyId 기반 조회 (Word 엔티티 함께 로딩 - LAZY 로딩 문제 해결) */
    @Query("SELECT swl FROM StoryWordList swl JOIN FETCH swl.word WHERE swl.storyId = :storyId")
    List<StoryWordList> findByStoryIdWithWord(@Param("storyId") Long storyId);

    /** wrongWordId 기반 조회 - 특정 오답 기록이 포함된 스토리 조회 */
    List<StoryWordList> findByWrongWordId(Long wrongWordId);

    /** 특정 스토리에 특정 오답 기록이 이미 포함되어 있는지 검사 */
    boolean existsByStoryIdAndWrongWordId(Long storyId, Long wrongWordId);

    /** wrongWordId를 NULL로 업데이트 (Hibernate 영속성 문제 회피) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE StoryWordList swl SET swl.wrongWordId = NULL WHERE swl.wrongWordId = :wrongWordId")
    int updateWrongWordIdToNull(@Param("wrongWordId") Long wrongWordId);
}
