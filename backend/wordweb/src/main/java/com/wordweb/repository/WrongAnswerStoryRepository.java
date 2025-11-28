package com.wordweb.repository;

import com.wordweb.entity.User;
import com.wordweb.entity.WrongAnswerStory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WrongAnswerStoryRepository extends JpaRepository<WrongAnswerStory, Long> {

    /** 유저별 스토리 전체 조회 */
    List<WrongAnswerStory> findByUser(User user);

    /** 유저별 최신 스토리 조회 */
    List<WrongAnswerStory> findByUserOrderByCreatedAtDesc(User user);

    /** 유저가 특정 제목을 가진 스토리를 이미 생성했는지 검사 (필요 시) */
    boolean existsByUserAndTitle(User user, String title);
}
