package com.wordweb.repository;

import com.wordweb.entity.UserWord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWordRepository extends JpaRepository<UserWord, Long> {

}
