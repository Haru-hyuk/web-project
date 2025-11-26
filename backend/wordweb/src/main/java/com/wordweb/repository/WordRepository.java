package com.wordweb.repository;

import com.wordweb.entity.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {

    /** 검색 */
    Page<Word> findByWordContainingIgnoreCase(String keyword, Pageable pageable);

    /** 품사 + 검색 */
    Page<Word> findByWordContainingIgnoreCaseAndPartOfSpeech(String keyword, String partOfSpeech, Pageable pageable);

    /** 카테고리 */
    Page<Word> findByCategory(String category, Pageable pageable);

    /** 레벨 */
    Page<Word> findByLevel(Integer level, Pageable pageable);

    /** 카테고리 + 레벨 */
    Page<Word> findByCategoryAndLevel(String category, Integer level, Pageable pageable);

    /** 랜덤 단어 하나 가져오기 */
    @Query(value = "SELECT * FROM WORD ORDER BY DBMS_RANDOM.VALUE FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    Word findRandomWord();

    /** 특정 index 기반 랜덤(정확히는 offset 기반) */
    @Query(value = "SELECT * FROM WORD OFFSET :index ROWS FETCH NEXT 1 ROWS ONLY", nativeQuery = true)
    Word findRandomWord(int index);

    /** 랜덤 n개 단어 (스토리 생성용) */
    @Query(value = "SELECT * FROM WORD ORDER BY DBMS_RANDOM.VALUE FETCH FIRST :count ROWS ONLY", nativeQuery = true)
    List<Word> findRandomWords(int count);
}
