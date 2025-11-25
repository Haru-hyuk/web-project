package com.wordweb.repository;

import com.wordweb.entity.WordProgress;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface WordProgressRepository extends JpaRepository<WordProgress, Long> {

    // 특정 유저 + 특정 단어의 학습 상태 조회
    Optional<WordProgress> findByUserAndWord(User user, Word word);

    // 유저의 모든 학습 기록
    List<WordProgress> findByUser(User user);

    // 오늘 학습한 단어 수 조회 (서비스에서 filtered 처리)
}
