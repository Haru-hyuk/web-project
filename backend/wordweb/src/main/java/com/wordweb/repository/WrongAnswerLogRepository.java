package com.wordweb.repository;

import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.WrongAnswerLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WrongAnswerLogRepository extends JpaRepository<WrongAnswerLog, Long> {

    List<WrongAnswerLog> findByUser(User user);

    List<WrongAnswerLog> findByWord(Word word);

    List<WrongAnswerLog> findByUserAndIsUsedInStoryFalse(User user);

    List<WrongAnswerLog> findByUserAndIsUsedInStoryTrue(User user);

    Optional<WrongAnswerLog> findByUserAndWord(User user, Word word);

    List<WrongAnswerLog> findByUserAndIsUsedInStory(User user, Boolean isUsedInStory);

    long countByUser(User user);

    /** 🔥 최신 오답 n개 (wrongAt desc) */
    Page<WrongAnswerLog> findByUserOrderByWrongAtDesc(User user, Pageable pageable);

    /** 🔥 오답 Top 5 (word 기준) */
    @Query("""
        SELECT new map(
            w.wordId as wordId,
            w.word as word,
            COUNT(l) as count
        )
        FROM WrongAnswerLog l
        JOIN l.word w
        WHERE l.user.userId = :userId
        GROUP BY w.wordId, w.word
        ORDER BY COUNT(l) DESC
        """)
    List<Map<String,Object>> findTop5GroupByWord(Long userId);
}
