package com.wordweb.repository;

import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.WrongAnswerLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WrongAnswerLogRepository extends JpaRepository<WrongAnswerLog, Long> {

    /** 유저별 오답 리스트 */
    List<WrongAnswerLog> findByUser(User user);

    /** 특정 단어의 오답 기록 찾기 */
    List<WrongAnswerLog> findByWord(Word word);

    /** 스토리에 사용되지 않은 오답만 */
    List<WrongAnswerLog> findByUserAndIsUsedInStoryFalse(User user);

    /** 스토리에 사용된 오답만 (옵션) */
    List<WrongAnswerLog> findByUserAndIsUsedInStoryTrue(User user);

    /** 유저 + 단어 조합으로 하나 검색 */
    Optional<WrongAnswerLog> findByUserAndWord(User user, Word word);

    /** 유저 단위로 스토리 미사용 오답 찾기 (string 버전 아님!) */
    List<WrongAnswerLog> findByUserAndIsUsedInStory(User user, Boolean isUsedInStory);
    
    long countByUser(User user);

}
