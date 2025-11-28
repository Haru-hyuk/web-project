package com.wordweb.repository;

import com.wordweb.entity.CompletedWord;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompletedWordRepository extends JpaRepository<CompletedWord, Long> {

    /** 유저가 특정 단어를 완료했는지 여부 */
    boolean existsByUserAndWord(User user, Word word);

    /** 유저가 완료한 모든 단어 목록 */
    List<CompletedWord> findByUser(User user);

    /** 특정 단어를 완료한 사용자 목록 (분석용) */
    List<CompletedWord> findByWord(Word word);

    /** 유저 + 단어 조합으로 하나 조회 */
    Optional<CompletedWord> findByUserAndWord(User user, Word word);
}
