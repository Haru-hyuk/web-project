package com.wordweb.controller;

import com.wordweb.dto.dashboard.DashboardResponse;
import com.wordweb.service.DashboardService;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {

        // 🔥 SecurityUtil은 static 메소드니까 직접 호출
        String email = SecurityUtil.getCurrentUserEmail();

        DashboardResponse response = dashboardService.getDashboard(email);
        return ResponseEntity.ok(response);
    }
}
