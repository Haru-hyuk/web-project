package com.wordweb.repository;

import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.WrongAnswerLog;
import com.wordweb.dto.dashboard.WrongTop5Dto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WrongAnswerLogRepository extends JpaRepository<WrongAnswerLog, Long> {

    List<WrongAnswerLog> findByUser(User user);

    List<WrongAnswerLog> findByWord(Word word);

    List<WrongAnswerLog> findByUserAndIsUsedInStoryFalse(User user);

    List<WrongAnswerLog> findByUserAndIsUsedInStoryTrue(User user);

    List<WrongAnswerLog> findByUserAndIsUsedInStory(User user, Boolean isUsed);

    long countByUser(User user);

    Optional<WrongAnswerLog> findByUserAndWord(User user, Word word);

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



    /** 이번 주 날짜 리스트(✔ 타입 수정됨) */
    @Query("""
    	    SELECT DISTINCT DATE(s.lastStudyAt)
    	    FROM StudyLog s
    	    WHERE s.user = :user
    	      AND s.lastStudyAt BETWEEN :start AND :end
    	""")
    	List<LocalDate> findStudyDatesBetween(
    	        User user,
    	        LocalDateTime start,
    	        LocalDateTime end
    	);
}
