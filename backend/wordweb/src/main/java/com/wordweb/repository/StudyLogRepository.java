package com.wordweb.repository;

import com.wordweb.entity.StudyLog;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
