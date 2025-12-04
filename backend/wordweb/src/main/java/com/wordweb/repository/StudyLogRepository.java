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

    /** 유저 + 단어 */
    Optional<StudyLog> findByUserAndWord(User user, Word word);

    /** 유저 전체 기록 */
    List<StudyLog> findByUser(User user);

    /** 단어 전체 기록 */
    List<StudyLog> findByWord(Word word);

    /** 상태 기반 */
    List<StudyLog> findByUserAndStatus(User user, String status);


    // =========================================================
    //                     DASHBOARD 용 쿼리 (MySQL)
    // =========================================================


    /** ⭐ 오늘 학습 완료 수 (MySQL CURRENT_DATE 사용) */
    @Query("""
        SELECT COUNT(s)
        FROM StudyLog s
        WHERE s.user.userId = :userId
          AND DATE(s.lastStudyAt) = CURRENT_DATE
    """)
    int countTodayCompleted(@Param("userId") Long userId);


    /** ⭐ 특정 날짜 학습 건수 */
    @Query("""
        SELECT COUNT(s)
        FROM StudyLog s
        WHERE s.user.userId = :userId
          AND DATE(s.lastStudyAt) = :targetDate
    """)
    int countByUserAndDate(
            @Param("userId") Long userId,
            @Param("targetDate") LocalDate targetDate
    );


    /** ⭐ streak 계산용 (정확한 날짜 일치 여부) */
    @Query("""
        SELECT COUNT(s)
        FROM StudyLog s
        WHERE s.user.userId = :userId
          AND DATE(s.lastStudyAt) = :date
    """)
    int countByUserAndExactDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

}
