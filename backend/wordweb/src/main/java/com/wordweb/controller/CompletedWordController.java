package com.wordweb.controller;

import com.wordweb.entity.CompletedWord;
import com.wordweb.service.CompletedWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/completed")
public class CompletedWordController {

    private final CompletedWordService completedWordService;

    /** 
     * 단어 학습 완료 처리 
     * POST /api/completed/{wordId}
     */
    @PostMapping("/{wordId}")
    public ResponseEntity<String> markCompleted(@PathVariable Long wordId) {
        completedWordService.markCompleted(wordId);
        return ResponseEntity.ok("학습 완료로 등록되었습니다.");
    }

    /**
     * 내가 완료한 단어 전체 조회
     * GET /api/completed
     */
    @GetMapping
    public ResponseEntity<List<CompletedWord>> getMyCompletedWords() {
        return ResponseEntity.ok(completedWordService.getMyCompletedWords());
    }

    /**
     * 특정 단어가 완료 상태인지 여부 확인
     * GET /api/completed/{wordId}/status
     */
    @GetMapping("/{wordId}/status")
    public ResponseEntity<Boolean> isCompleted(@PathVariable Long wordId) {
        return ResponseEntity.ok(completedWordService.isCompleted(wordId));
    }
}
