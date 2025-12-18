package com.wordweb.controller;

import com.wordweb.dto.word.WordResponse;
import com.wordweb.service.FlashcardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcard")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    /**
     * 플래시카드용 단어 목록 가져오기
     * GET /api/flashcard?count=20&level=beginner&category=Daily Life&wordIds=1,2,3
     */
    @GetMapping
    public ResponseEntity<List<WordResponse>> getFlashcardWords(
            @RequestParam(required = false, defaultValue = "10") Integer count,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<Long> wordIds
    ) {
        return ResponseEntity.ok(flashcardService.getFlashcardWords(count, level, category, wordIds));
    }

    /**
     * 오답 플래시카드용 단어 목록 가져오기
     * GET /api/flashcard/wrong?count=20
     */
    @GetMapping("/wrong")
    public ResponseEntity<List<WordResponse>> getWrongAnswerFlashcardWords(
            @RequestParam(required = false) Integer count
    ) {
        return ResponseEntity.ok(flashcardService.getWrongAnswerFlashcardWords(count));
    }
}
