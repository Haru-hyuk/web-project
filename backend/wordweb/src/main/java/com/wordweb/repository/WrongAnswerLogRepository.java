package com.wordweb.repository;

import com.wordweb.dto.dashboard.WrongTop5Dto;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.WrongAnswerLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WrongAnswerLogRepository extends JpaRepository<WrongAnswerLog, Long> {

    /** 유저별 오답 리스트 */
    List<WrongAnswerLog> findByUser(User user);

    /** 유저별 오답 리스트 (Word 엔티티 함께 로딩 - N+1 문제 해결) */
    @Query("SELECT wl FROM WrongAnswerLog wl JOIN FETCH wl.word WHERE wl.user = :user")
    List<WrongAnswerLog> findByUserWithWord(@Param("user") User user);

    /** 특정 단어의 오답 기록 찾기 */
    List<WrongAnswerLog> findByWord(Word word);

    /** 스토리에 사용되지 않은 오답만 */
    List<WrongAnswerLog> findByUserAndIsUsedInStoryFalse(User user);

    /** 스토리에 사용되지 않은 오답만 (Word 엔티티 함께 로딩 - LAZY 로딩 문제 해결) */
    @Query("SELECT wl FROM WrongAnswerLog wl JOIN FETCH wl.word WHERE wl.user = :user AND wl.isUsedInStory = false")
    List<WrongAnswerLog> findByUserAndIsUsedInStoryFalseWithWord(@Param("user") User user);

    /** 스토리에 사용된 오답만 */
    List<WrongAnswerLog> findByUserAndIsUsedInStoryTrue(User user);

    /** 유저 + 단어 조합으로 하나 검색 */
    Optional<WrongAnswerLog> findByUserAndWord(User user, Word word);

    /** 유저 단위로 스토리 미사용 오답 찾기 */
    List<WrongAnswerLog> findByUserAndIsUsedInStory(User user, Boolean isUsedInStory);

    long countByUser(User user);

    /** ID 목록으로 조회 (Word 엔티티 함께 로딩 - N+1 문제 해결) */
    @Query("SELECT wl FROM WrongAnswerLog wl JOIN FETCH wl.word WHERE wl.wrongWordId IN :ids")
    List<WrongAnswerLog> findAllByIdWithWord(@Param("ids") List<Long> ids);

    /** 최근 오답 순으로 조회 (페이징) */
    Page<WrongAnswerLog> findByUserOrderByWrongAtDesc(User user, Pageable pageable);

    /** TOP 5 오답 단어 */
    @Query("""
            SELECT new com.wordweb.dto.dashboard.WrongTop5Dto(
                w.wordId,
                w.word,
                COUNT(l)
            )
            FROM WrongAnswerLog l
            JOIN l.word w
            WHERE l.user.userId = :userId
            GROUP BY w.wordId, w.word
            ORDER BY COUNT(l) DESC
        """)
    List<WrongTop5Dto> findTop5GroupByWord(@Param("userId") Long userId);

}
