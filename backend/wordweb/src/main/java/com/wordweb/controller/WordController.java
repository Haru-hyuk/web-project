package com.wordweb.controller;

import com.wordweb.dto.word.WordResponse;
import com.wordweb.repository.WordRepository;
import com.wordweb.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;
    private final WordRepository wordRepository; // 추가!!!

    /** 단어 상세 조회 */
    @GetMapping("/detail/{wordId}")
    public ResponseEntity<WordResponse> getWord(@PathVariable Long wordId) {
        return ResponseEntity.ok(wordService.getWord(wordId));
    }

    /** 오늘의 단어 */
    @GetMapping("/today")
    public ResponseEntity<WordResponse> getTodayWord() {
        return ResponseEntity.ok(wordService.getTodayWord());
    }

    /** 전체 단어 목록 */
    @GetMapping
    public ResponseEntity<Page<WordResponse>> getWordList(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(wordService.getWordList(pageable));
    }

    /** 검색 (keyword) */
    @GetMapping("/search")
    public ResponseEntity<Page<WordResponse>> searchWords(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(wordService.searchWords(keyword, pageable));
    }

    /** 필터 검색 */
    @GetMapping("/filter")
    public ResponseEntity<Page<WordResponse>> filterWords(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String partOfSpeech,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                wordService.filterWords(category, level, partOfSpeech, pageable)
        );
    }

    /** 테스트: 전체 단어 개수 확인 */
    @GetMapping("/test-count")
    public Long testCount() {
        return wordRepository.count();  // ← 이게 정답
    }
}
