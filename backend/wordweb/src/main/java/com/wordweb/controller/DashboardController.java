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

    @GetMapping
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/daily-goal")
    public ResponseEntity<?> getDailyGoal() {
        return ResponseEntity.ok(dashboardService.getDailyGoal());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyStats() {
        return ResponseEntity.ok(dashboardService.getWeeklyStats());
    }

    @GetMapping("/wrong/top5")
    public ResponseEntity<?> getWrongTop5() {
        try {
            return ResponseEntity.ok(dashboardService.getWrongTop5());
        } catch (Exception e) {
            e.printStackTrace(); // ← 실제 에러 출력
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
                "week", List.of("M","T","W","T","F","S","S"),
                "checked", weekly
        ));
    }
    
    
}
