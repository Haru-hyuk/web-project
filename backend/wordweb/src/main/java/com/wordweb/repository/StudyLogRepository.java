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
     * Hibernate 7에서 TRUNC(TIMESTAMP) 사용 불가 → BETWEEN 방식 사용
     * ===================== */

    /** 오늘 학습 완료 수 */
    @Query(value = """
    	    SELECT COUNT(*) 
    	    FROM STUDY_LOG 
    	    WHERE USER_ID = :userId
    	      AND LAST_STUDY_AT >= TRUNC(SYSDATE)
    	      AND LAST_STUDY_AT < TRUNC(SYSDATE) + 1
    	""", nativeQuery = true)
    	int countTodayCompleted(@Param("userId") Long userId);


    /** 특정 날짜 학습 건수 */
    @Query(value = """
    	    SELECT COUNT(*) 
    	    FROM STUDY_LOG 
    	    WHERE USER_ID = :userId
    	      AND LAST_STUDY_AT >= TRUNC(:targetDate)
    	      AND LAST_STUDY_AT < TRUNC(:targetDate) + 1
    	""", nativeQuery = true)
    	int countByUserAndDate(
    	        @Param("userId") Long userId,
    	        @Param("targetDate") LocalDate targetDate
    	);


}
