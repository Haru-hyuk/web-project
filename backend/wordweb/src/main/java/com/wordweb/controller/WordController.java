package com.wordweb.controller;

import com.wordweb.dto.word.WordResponse;
import com.wordweb.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    /** 단어 1개 상세 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<WordResponse> getWord(@PathVariable Long id) {
        return ResponseEntity.ok(wordService.getWord(id));
    }

    /** 오늘의 단어 조회 */
    @GetMapping("/today")
    public ResponseEntity<WordResponse> getTodayWord() {
        return ResponseEntity.ok(wordService.getTodayWord());
    }

    /** 전체 단어 조회 (페이지네이션) */
    @GetMapping
    public ResponseEntity<Page<WordResponse>> getWords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(wordService.getWordList(page, size));
    }

    /** 단어 검색 (keyword) */
    @GetMapping("/search")
    public ResponseEntity<Page<WordResponse>> searchWords(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(wordService.searchWords(keyword, page, size));
    }
}
