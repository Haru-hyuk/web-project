package com.wordweb.repository;

import com.wordweb.entity.StudyLog;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
     * MySQL용: DATE() 함수 사용 (Oracle의 TRUNC 대신)
     * ===================== */

    /** 오늘 학습 완료 수 */
    @Query(value = """
    	    SELECT COUNT(*) 
    	    FROM STUDY_LOG 
    	    WHERE USER_ID = :userId
    	      AND DATE(LAST_STUDY_AT) = CURDATE()
    	""", nativeQuery = true)
    	int countTodayCompleted(@Param("userId") Long userId);


    /** 특정 날짜 학습 건수 */
    @Query(value = """
    	    SELECT COUNT(*)
    	    FROM STUDY_LOG
    	    WHERE USER_ID = :userId
    	      AND DATE(LAST_STUDY_AT) = DATE(:targetDate)
    	""", nativeQuery = true)
    	int countByUserAndDate(
    	        @Param("userId") Long userId,
    	        @Param("targetDate") LocalDate targetDate
    	);

    /** 이번 주 날짜 리스트 */
    @Query(value = """
            SELECT DISTINCT DATE(LAST_STUDY_AT)
            FROM STUDY_LOG
            WHERE USER_ID = :userId
              AND LAST_STUDY_AT BETWEEN :start AND :end
        """, nativeQuery = true)
    List<LocalDate> findStudyDatesBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /** 오답 횟수 기준 Top 5 단어 (study_log 기반) */
    @Query("""
            SELECT s.word.wordId as wordId,
                   s.word.word as word,
                   s.word.meaning as meaning,
                   SUM(s.totalWrong) as wrongCount
            FROM StudyLog s
            WHERE s.user.userId = :userId
              AND s.totalWrong > 0
            GROUP BY s.word.wordId, s.word.word, s.word.meaning
            ORDER BY wrongCount DESC
            LIMIT 5
        """)
    List<Object[]> findTop5ByTotalWrong(@Param("userId") Long userId);

}
