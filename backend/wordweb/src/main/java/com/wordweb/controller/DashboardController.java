package com.wordweb.controller;

import com.wordweb.service.DashboardService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    /** 0) 대시보드 메인 */
    @GetMapping
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    /** 1) 오늘 목표 */
    @GetMapping("/daily-goal")
    public ResponseEntity<?> getDailyGoal() {
        return ResponseEntity.ok(dashboardService.getDailyGoal());
    }

    /** 2) 전체 통계 */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    /** 3) 최근 7일 학습량 */
    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyStats() {
        return ResponseEntity.ok(dashboardService.getWeeklyStats());
    }

    /** 4) 오답 Top 5 */
    @GetMapping("/wrong/top5")
    public ResponseEntity<?> getWrongTop5() {
        return ResponseEntity.ok(dashboardService.getWrongTop5());
    }

    /** 5) 오답 복습 리스트 (limit 기본 10) */
    @GetMapping("/wrong/review")
    public ResponseEntity<?> getWrongReview(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getWrongReview(limit));
    }
    
    @GetMapping("/week-study")
    public ResponseEntity<?> getWeeklyStudyStatus() {
        List<Boolean> weekly = dashboardService.getWeeklyStudyStatus();
        return ResponseEntity.ok(Map.of(
                "week", List.of("M","T","W","T","F","S","S"),
                "checked", weekly
        ));
    }


}
	