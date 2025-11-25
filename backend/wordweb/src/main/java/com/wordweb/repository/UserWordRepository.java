package com.wordweb.repository;

import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.UserWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserWordRepository extends JpaRepository<UserWord, Long> {

    Optional<UserWord> findByUserAndWord(User user, Word word);

    List<UserWord> findAllByUser(User user);

    boolean existsByUserAndWord(User user, Word word);
}
