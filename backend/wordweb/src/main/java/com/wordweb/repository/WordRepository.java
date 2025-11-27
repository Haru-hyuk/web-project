package com.wordweb.repository;

import com.wordweb.entity.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {

    Page<Word> findByWordContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Word> findByWordContainingIgnoreCaseAndPartOfSpeech(String keyword, String partOfSpeech, Pageable pageable);

    Page<Word> findByCategory(String category, Pageable pageable);

    // ★ wordLevel로 수정
    Page<Word> findByWordLevel(Integer wordLevel, Pageable pageable);

    // ★ wordLevel로 수정
    Page<Word> findByCategoryAndWordLevel(String category, Integer wordLevel, Pageable pageable);

    @Query(value = "SELECT * FROM WORD ORDER BY DBMS_RANDOM.VALUE FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    Word findRandomWord();

    @Query(value = "SELECT * FROM WORD OFFSET :index ROWS FETCH NEXT 1 ROWS ONLY", nativeQuery = true)
    Word findRandomWord(int index);

    @Query(value = "SELECT * FROM WORD ORDER BY DBMS_RANDOM.VALUE FETCH FIRST :count ROWS ONLY", nativeQuery = true)
    List<Word> findRandomWords(int count);
}
