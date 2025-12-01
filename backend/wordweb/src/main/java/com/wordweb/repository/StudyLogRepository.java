package com.wordweb.repository;

import com.wordweb.entity.StudyLog;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {

    /** 유저 + 단어 조합으로 StudyLog 하나 조회 */
    Optional<StudyLog> findByUserAndWord(User user, Word word);

    /** 유저의 모든 학습 기록 */
    List<StudyLog> findByUser(User user);

    /** 특정 단어의 학습 기록 전체 */
    List<StudyLog> findByWord(Word word);

    /** 특정 유저의 학습 상태 기반 조회 (예: learned/pending) */
    List<StudyLog> findByUserAndStatus(User user, String status);

    /** ======================
     * Dashboard 용 커스텀 쿼리
     * ===================== */

    /** 오늘 학습 완료 수 */
    @Query("SELECT COUNT(s) FROM StudyLog s " +
           "WHERE s.user.userId = :userId AND DATE(s.updatedAt) = CURRENT_DATE")
    int countTodayCompleted(@Param("userId") Long userId);

    /** 특정 날짜 학습 건수 (주간 학습 그래프용) */
    @Query("SELECT COUNT(s) FROM StudyLog s " +
           "WHERE s.user.userId = :userId AND DATE(s.updatedAt) = :targetDate")
    int countByUserAndDate(@Param("userId") Long userId,
                           @Param("targetDate") LocalDate targetDate);
}
