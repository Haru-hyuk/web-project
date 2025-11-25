package com.wordweb.controller;

import com.wordweb.dto.progress.ProgressUpdateRequest;
import com.wordweb.service.WordProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/progress")
public class WordProgressController {

    private final WordProgressService progressService;

    /** 단어 학습 상태 저장/변경 */
    @PostMapping("/{wordId}")
    public ResponseEntity<String> updateProgress(
            @PathVariable Long wordId,
            @RequestBody ProgressUpdateRequest request
    ) {
        progressService.updateProgress(wordId, request);
        return ResponseEntity.ok("학습 상태가 업데이트되었습니다.");
    }

    /** 특정 단어의 학습 상태 조회 */
    @GetMapping("/{wordId}")
    public ResponseEntity<String> getLearningStatus(
            @PathVariable Long wordId
    ) {
        return ResponseEntity.ok(progressService.getLearningStatus(wordId));
    }

    /** 오늘 학습한 단어 수 */
    @GetMapping("/today/count")
    public ResponseEntity<Integer> getTodayCount() {
        return ResponseEntity.ok(progressService.getTodayCount());
    }

    /** 최근 7일 학습량 */
    @GetMapping("/week/count")
    public ResponseEntity<Integer> getWeekCount() {
        return ResponseEntity.ok(progressService.getWeekCount());
    }

    /** 최근 30일 학습량 */
    @GetMapping("/month/count")
    public ResponseEntity<Integer> getMonthCount() {
        return ResponseEntity.ok(progressService.getMonthCount());
    }
}
