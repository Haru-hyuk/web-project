package com.wordweb.repository;

import com.wordweb.entity.FavoriteWord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteWordRepository extends JpaRepository<FavoriteWord, Long> {

}
