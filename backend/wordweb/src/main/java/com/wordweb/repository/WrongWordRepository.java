package com.wordweb.repository;

import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.WrongWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WrongWordRepository extends JpaRepository<WrongWord, Long> {

    boolean existsByUserAndWord(User user, Word word);

    List<WrongWord> findAllByUser(User user);

    Optional<WrongWord> findByUserAndWord(User user, Word word);
}
