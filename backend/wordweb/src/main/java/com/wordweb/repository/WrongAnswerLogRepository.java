package com.wordweb.repository;

import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.WrongAnswerLog;
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

    /** 스토리에 사용되지 않은 오답만 (Word 엔티티 함께 로딩 - LAZY 로딩 문제 해결) */
    @Query("SELECT wl FROM WrongAnswerLog wl JOIN FETCH wl.word WHERE wl.user = :user AND wl.isUsedInStory = false")
    List<WrongAnswerLog> findByUserAndIsUsedInStoryFalseWithWord(@Param("user") User user);

    /** 유저 + 단어 조합으로 하나 검색 */
    Optional<WrongAnswerLog> findByUserAndWord(User user, Word word);

    long countByUser(User user);

    /** ID 목록으로 조회 (Word 엔티티 함께 로딩 - N+1 문제 해결) */
    @Query("SELECT wl FROM WrongAnswerLog wl JOIN FETCH wl.word WHERE wl.wrongWordId IN :ids")
    List<WrongAnswerLog> findAllByIdWithWord(@Param("ids") List<Long> ids);

}
