package com.wordweb.controller;

import com.wordweb.dto.progress.StudyLogResponse;
import com.wordweb.service.StudyLogService;
import com.wordweb.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyLogController {

    private final StudyLogService studyLogService;
    private final DashboardService dashboardService;

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

    /** 연속 학습일 조회 */
    @GetMapping("/streak")
    public ResponseEntity<StreakResponse> getStreak() {
        int streak = dashboardService.getStreak();
        return ResponseEntity.ok(new StreakResponse(streak));
    }

    /** DTO: 연속 학습일 응답 */
    public static class StreakResponse {
        private int streak;

        public StreakResponse(int streak) {
            this.streak = streak;
        }

        public int getStreak() {
            return streak;
        }

        public void setStreak(int streak) {
            this.streak = streak;
        }
    }
}
	