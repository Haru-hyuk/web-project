package com.wordweb.controller;

import com.wordweb.dto.progress.StudyLogResponse;
import com.wordweb.service.StudyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyLogController {

    private final StudyLogService studyLogService;

    /** 정답 처리 */
    @PostMapping("/{wordId}/correct")
    public ResponseEntity<StudyLogResponse> markCorrect(@PathVariable Long wordId) {
        return ResponseEntity.ok(studyLogService.markCorrect(wordId));
    }

    /** 오답 처리 */
    @PostMapping("/{wordId}/wrong")
    public ResponseEntity<StudyLogResponse> markWrong(@PathVariable Long wordId) {
        return ResponseEntity.ok(studyLogService.markWrong(wordId));
    }

    /** 단어 학습 상태 조회 */
    @GetMapping("/{wordId}/status")
    public ResponseEntity<String> getStatus(@PathVariable Long wordId) {
        return ResponseEntity.ok(studyLogService.getStatus(wordId));
    }
}
