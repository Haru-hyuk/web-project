package com.wordweb.controller;

import com.wordweb.entity.WrongAnswerLog;
import com.wordweb.service.WrongAnswerLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wrong")
@RequiredArgsConstructor
public class WrongAnswerLogController {

    private final WrongAnswerLogService wrongAnswerLogService;

    /** 오답 추가 */
    @PostMapping("/{wordId}")
    public ResponseEntity<String> addWrongAnswer(@PathVariable Long wordId) {
        wrongAnswerLogService.addWrongAnswer(wordId);
        return ResponseEntity.ok("오답 단어로 등록되었습니다.");
    }

    /** 오답 삭제 (필요 시) */
    @DeleteMapping("/{wordId}")
    public ResponseEntity<String> removeWrongAnswer(@PathVariable Long wordId) {
        wrongAnswerLogService.removeWrongAnswer(wordId);
        return ResponseEntity.ok("오답 단어에서 제거되었습니다.");
    }

    /** 오답 목록 조회 */
    @GetMapping
    public ResponseEntity<List<WrongAnswerLog>> getMyWrongLogs() {
        return ResponseEntity.ok(wrongAnswerLogService.getMyWrongLogs());
    }

    /** 스토리에 사용되지 않은 오답 목록 */
    @GetMapping("/unused")
    public ResponseEntity<List<WrongAnswerLog>> getUnusedWrongLogs() {
        return ResponseEntity.ok(wrongAnswerLogService.getUnusedWrongLogs());
    }

    /** 특정 오답을 스토리에서 사용됨 처리 */
    @PostMapping("/mark-used/{wrongLogId}")
    public ResponseEntity<String> markUsedInStory(@PathVariable Long wrongLogId) {
        wrongAnswerLogService.markUsedInStory(wrongLogId);
        return ResponseEntity.ok("스토리 사용으로 업데이트되었습니다.");
    }
}
