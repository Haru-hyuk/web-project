package com.wordweb.repository;

import com.wordweb.entity.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {

    /** keyword 검색 */
    Page<Word> findByWordContainingIgnoreCase(String keyword, Pageable pageable);

    /** 품사 + keyword 검색 */
    Page<Word> findByWordContainingIgnoreCaseAndPartOfSpeech(
            String keyword,
            String partOfSpeech,
            Pageable pageable
    );

    /** 카테고리 필터 */
    Page<Word> findByCategory(String category, Pageable pageable);

    /** 레벨 필터 */
    Page<Word> findByLevel(String level, Pageable pageable);

    /** 카테고리 + 레벨 복합 필터 */
    Page<Word> findByCategoryAndLevel(String category, String level, Pageable pageable);
}
