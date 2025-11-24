package com.wordweb.repository;

import com.wordweb.entity.WrongWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WrongWordRepository extends JpaRepository<WrongWord, Long> {

    List<WrongWord> findByIsUsedInStory(String isUsedInStory);

    List<WrongWord> findByUser_UserIdAndIsUsedInStory(Long userId, String isUsedInStory);
}
