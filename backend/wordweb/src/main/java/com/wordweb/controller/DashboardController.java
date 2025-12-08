package com.wordweb.controller;

import com.wordweb.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    /** 오늘 목표 */
    @GetMapping("/daily-goal")
    public ResponseEntity<?> getDailyGoal() {
        return ResponseEntity.ok(dashboardService.getDailyGoal());
    }

    /** 전체 통계 */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    /** 최근 7일 학습량 */
    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyStats() {
        return ResponseEntity.ok(dashboardService.getWeeklyStats());
    }

    @GetMapping("/wrong/top5")
    public ResponseEntity<?> getWrongTop5() {
        try {
            return ResponseEntity.ok(dashboardService.getWrongTop5());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/wrong/review")
    public ResponseEntity<?> getWrongReview(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(dashboardService.getWrongReview(limit));
    }

    @GetMapping("/week-study")
    public ResponseEntity<?> getWeeklyStudyStatus() {
        List<Boolean> weekly = dashboardService.getWeeklyStudyStatus();
        return ResponseEntity.ok(Map.of(
                "week", List.of("S","M","T","W","T","F","S"), // 일요일부터 시작
                "checked", weekly
        ));
    }
}
