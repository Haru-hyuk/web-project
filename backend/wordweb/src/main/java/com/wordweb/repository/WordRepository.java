package com.wordweb.repository;

import com.wordweb.entity.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {

    /** 단어 검색 */
    Page<Word> findByWordContainingIgnoreCase(String keyword, Pageable pageable);

    /** 단어 + 품사 검색 */
    Page<Word> findByWordContainingIgnoreCaseAndPartOfSpeech(
            String keyword,
            String partOfSpeech,
            Pageable pageable
    );

    /** 카테고리 검색 */
    Page<Word> findByCategory(String category, Pageable pageable);

    /** 난이도 검색 */
    Page<Word> findByLevel(Integer level, Pageable pageable);

    /** 카테고리 + 난이도 검색 */
    Page<Word> findByCategoryAndLevel(String category, Integer level, Pageable pageable);

    /** 품사 검색 */
    Page<Word> findByPartOfSpeech(String partOfSpeech, Pageable pageable);

    /** 카테고리 + 품사 */
    Page<Word> findByCategoryAndPartOfSpeech(String category, String partOfSpeech, Pageable pageable);

    /** 레벨 + 품사 */
    Page<Word> findByLevelAndPartOfSpeech(Integer level, String partOfSpeech, Pageable pageable);

    /** 카테고리 + 레벨 + 품사 */
    Page<Word> findByCategoryAndLevelAndPartOfSpeech(String category, Integer level, String partOfSpeech, Pageable pageable);

    /** 정확한 단어 매칭 */
    Optional<Word> findByWord(String word);

    /** 같은 품사 + 같은 레벨 */
    List<Word> findByPartOfSpeechAndLevel(String partOfSpeech, Integer level);

    /** 같은 품사 */
    List<Word> findByPartOfSpeech(String partOfSpeech);

    /** 카테고리로 필터링 (List 반환) - 퀴즈/플래시카드용 */
    List<Word> findByCategory(String category);

    /** 레벨 범위로 필터링 (List 반환) - 퀴즈 최적화용 */
    @Query("SELECT w FROM Word w WHERE w.level IN :levels")
    List<Word> findByLevelIn(@Param("levels") List<Integer> levels);

    /** 카테고리 + 레벨 범위로 필터링 (List 반환) - 퀴즈 최적화용 */
    @Query("SELECT w FROM Word w WHERE w.category = :category AND w.level IN :levels")
    List<Word> findByCategoryAndLevelIn(@Param("category") String category, @Param("levels") List<Integer> levels);
}
