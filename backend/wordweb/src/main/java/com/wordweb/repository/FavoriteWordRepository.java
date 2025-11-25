package com.wordweb.repository;

import com.wordweb.entity.FavoriteWord;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteWordRepository extends JpaRepository<FavoriteWord, Long> {

    // 이미 즐겨찾기 여부 확인
    boolean existsByUserAndWord(User user, Word word);

    // 즐겨찾기 목록 조회
    List<FavoriteWord> findByUser(User user);

    // 즐겨찾기 삭제
    void deleteByUserEmailAndWordWordId(String email, Long wordId);
}
