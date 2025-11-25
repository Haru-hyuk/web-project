package com.wordweb.repository;

import com.wordweb.entity.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {

    // keyword 검색
    Page<Word> findByWordContainingIgnoreCase(String keyword, Pageable pageable);

    // (선택) 품사 + keyword 검색
    Page<Word> findByWordContainingIgnoreCaseAndPartOfSpeech(
            String keyword,
            String partOfSpeech,
            Pageable pageable
    );
}
