package com.wordweb.service;

import com.wordweb.dto.dashboard.DashboardResponse;
import com.wordweb.entity.User;
import com.wordweb.repository.*;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final FavoriteWordRepository favoriteWordRepository;
    private final CompletedWordRepository completedWordRepository;
    private final WrongAnswerLogRepository wrongAnswerLogRepository;
    private final StudyLogRepository studyLogRepository;

    /** 현재 로그인 유저 조회 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    /** 대시보드 메인 API */
    public DashboardResponse getDashboard() {
        User user = getLoginUser();

        int dailyGoal = user.getDailyWordGoal();
        int completedToday = studyLogRepository.countTodayCompleted(user.getUserId());
        int percentage = (int) ((completedToday / (double) dailyGoal) * 100);

        return DashboardResponse.builder()
                .nickname(user.getNickname())
                .dailyGoal(dailyGoal)
                .todayProgress(completedToday)
                .percentage(percentage)
                .build();
    }

    /** 1) 오늘 목표 API */
    public Map<String, Object> getDailyGoal() {
        User user = getLoginUser();

        int goal = user.getDailyWordGoal();  
        int completedToday = studyLogRepository.countTodayCompleted(user.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("nickname", user.getNickname());
        result.put("dailyGoal", goal);
        result.put("completedToday", completedToday);
        result.put("progressRate", (int) ((completedToday / (double) goal) * 100));
        return result;
    }

    /** 2) 전체 통계 API */
    public Map<String, Object> getStats() {
        User user = getLoginUser();

        long totalWords = wordRepository.count();
        long favorites = favoriteWordRepository.countByUser(user);
        long completed = completedWordRepository.countByUser(user);
        long wrongAnswers = wrongAnswerLogRepository.countByUser(user);

        return Map.of(
                "totalWords", totalWords,
                "favoriteWords", favorites,
                "completedWords", completed,
                "wrongAnswers", wrongAnswers
        );
    }

    /** 3) 최근 7일 학습량 API */
    public List<Map<String, Object>> getWeeklyStats() {
        User user = getLoginUser();

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate target = today.minusDays(i);

            int count = studyLogRepository.countByUserAndDate(user.getUserId(), target);

            result.add(Map.of(
                    "date", target.toString(),
                    "count", count
            ));
        }

        return result;
    }
}
