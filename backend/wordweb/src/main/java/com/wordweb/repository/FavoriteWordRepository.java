package com.wordweb.repository;

import com.wordweb.entity.FavoriteWord;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteWordRepository extends JpaRepository<FavoriteWord, Long> {

    /** 유저 + 단어 조합으로 즐겨찾기 여부 확인 */
    boolean existsByUserAndWord(User user, Word word);

    /** 유저 즐겨찾기 전체 조회 */
    List<FavoriteWord> findByUser(User user);

    /** 즐겨찾기 삭제 */
    void deleteByUserAndWord(User user, Word word);
    
    long countByUser(User user);
}
