package com.wordweb.service;

import com.wordweb.dto.dashboard.DashboardResponse;
import com.wordweb.entity.User;
import com.wordweb.repository.*;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;


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
        int streak = getStreak();

        return DashboardResponse.builder()
                .nickname(user.getNickname())
                .dailyGoal(dailyGoal)
                .todayProgress(completedToday)
                .percentage(percentage)
                .streak(streak)
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
    
    public int getStreak() {
        User user = getLoginUser();
        Long userId = user.getUserId();

        int streak = 0;
        LocalDate today = LocalDate.now();

        while (true) {
            LocalDate target = today.minusDays(streak);
            int count = studyLogRepository.countByUserAndExactDate(userId, target);

            if (count > 0) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    public List<Map<String, Object>> getWrongTop5() {
        User user = getLoginUser();

        return wrongAnswerLogRepository.findTop5GroupByWord(user.getUserId());
    }
    
    public List<Map<String, Object>> getWrongReview(int limit) {
        User user = getLoginUser();

        return wrongAnswerLogRepository
                .findByUserOrderByWrongAtDesc(user, PageRequest.of(0, limit))
                .stream()
                .map(log -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("wordId", log.getWord().getWordId());
                    map.put("word", log.getWord().getWord());
                    map.put("meaning", log.getWord().getMeaning());
                    return map;
                })
                .toList();
    }
    
    /** 4) 이번 주 요일별 학습 여부 API */
    public List<Boolean> getWeeklyStudyStatus() {
        User user = getLoginUser();

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(java.time.DayOfWeek.SUNDAY);

        // 이번 주 학습한 날짜 목록
        List<LocalDate> studyDates = studyLogRepository.findStudyDatesBetween(
                user,
                startOfWeek.atStartOfDay(),
                endOfWeek.atTime(23, 59, 59)
        );

        // 월~일 총 7일 boolean 배열 생성
        List<Boolean> result = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            result.add(studyDates.contains(date));  // true / false
        }

        return result;
    }






}
