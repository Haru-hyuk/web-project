package com.wordweb.service;

import com.wordweb.dto.dashboard.DashboardResponse;
import com.wordweb.entity.User;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final WordProgressRepository wordProgressRepository;

    public DashboardResponse getDashboard(String email) {

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        int dailyGoal = user.getDailyWordGoal();

        // 오늘 학습한 단어 수
        int todayProgress = wordProgressRepository.countTodayProgress(user.getUserId());

        // 퍼센트 계산
        int percentage = (int) ((todayProgress / (double) dailyGoal) * 100);

        return DashboardResponse.builder()
                .nickname(user.getNickname())
                .dailyGoal(dailyGoal)
                .todayProgress(todayProgress)
                .percentage(percentage)
                .build();
    }
}
