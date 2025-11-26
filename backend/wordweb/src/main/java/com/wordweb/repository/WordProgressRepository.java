package com.wordweb.repository;

import com.wordweb.entity.WordProgress;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface WordProgressRepository extends JpaRepository<WordProgress, Long> {

    // 특정 유저 + 특정 단어의 학습 상태 조회
    Optional<WordProgress> findByUserAndWord(User user, Word word);

    // 유저의 전체 학습 기록 조회
    List<WordProgress> findByUser(User user);

    // 오늘 학습한 단어 수 조회
    @Query("SELECT COUNT(wp) FROM WordProgress wp " +
            "WHERE wp.user.userId = :userId " +
            "AND wp.studyDate = CURRENT_DATE")
    int countTodayProgress(@Param("userId") Long userId);
}
