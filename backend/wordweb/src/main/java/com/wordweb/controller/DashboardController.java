package com.wordweb.controller;

import com.wordweb.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
