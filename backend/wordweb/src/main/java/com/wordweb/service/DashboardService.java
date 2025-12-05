package com.wordweb.service;

import com.wordweb.dto.dashboard.DashboardResponse;
import com.wordweb.entity.User;
import com.wordweb.repository.*;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final FavoriteWordRepository favoriteWordRepository;
    private final CompletedWordRepository completedWordRepository;
    private final WrongAnswerLogRepository wrongAnswerLogRepository;
    private final StudyLogRepository studyLogRepository;

    /** 현재 로그인 유저 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    /** 메인 대시보드 */
    public DashboardResponse getDashboard() {
        User user = getLoginUser();

        int dailyGoal = user.getDailyWordGoal();
        int completedToday = studyLogRepository.countTodayCompleted(user.getUserId());
        int percentage = (int)((completedToday / (double) dailyGoal) * 100);
        int streak = getStreak();

        return DashboardResponse.builder()
                .nickname(user.getNickname())
                .dailyGoal(dailyGoal)
                .todayProgress(completedToday)
                .percentage(percentage)
                .streak(streak)
                .build();
    }

    /** 오늘 목표 */
    public Map<String, Object> getDailyGoal() {
        User user = getLoginUser();

        int goal = user.getDailyWordGoal();
        int completedToday = studyLogRepository.countTodayCompleted(user.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("nickname", user.getNickname());
        result.put("dailyGoal", goal);
        result.put("completedToday", completedToday);
        result.put("progressRate", (int)((completedToday / (double)goal) * 100));

        return result;
    }

    /** 전체 통계 */
    public Map<String, Object> getStats() {
        User user = getLoginUser();

        long totalWords = wordRepository.count();
        long favorites = favoriteWordRepository.countByUser(user);
        long completed = completedWordRepository.countByUser(user);
        long wrongAnswers = wrongAnswerLogRepository.countByUser(user);

        Map<String, Object> result = new HashMap<>();
        result.put("totalWords", totalWords);
        result.put("favoriteWords", favorites);
        result.put("completedWords", completed);
        result.put("wrongAnswers", wrongAnswers);

        return result;
    }

    /** 최근 7일 학습량 */
    public List<Map<String,Object>> getWeeklyStats() {
        User user = getLoginUser();

        List<Map<String,Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate target = today.minusDays(i);
            int count = studyLogRepository.countByUserAndDate(user.getUserId(), target);

            Map<String, Object> map = new HashMap<>();
            map.put("date", target.toString());
            map.put("count", count);

            result.add(map);
        }

        return result;
    }

    /** 연속 학습일 */
    public int getStreak() {
        User user = getLoginUser();
        Long userId = user.getUserId();

        int streak = 0;
        LocalDate today = LocalDate.now();

        while (true) {
            LocalDate target = today.minusDays(streak);
            int count = studyLogRepository.countByUserAndExactDate(userId, target);

            if (count > 0) streak++;
            else break;
        }

        return streak;
    }

    /** 오답 Top 5 */
    public List<Map<String,Object>> getWrongTop5() {
        User user = getLoginUser();

        return wrongAnswerLogRepository.findTop5GroupByWord(user.getUserId())
                .stream()
                .map(dto -> {
                    Map<String,Object> map = new HashMap<>();
                    map.put("wordId", dto.getWordId());
                    map.put("word", dto.getWord());
                    map.put("count", dto.getCount());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /** 오답 복습 리스트 */
    public List<Map<String,Object>> getWrongReview(int limit) {
        User user = getLoginUser();

        return wrongAnswerLogRepository
                .findByUserOrderByWrongAtDesc(user, PageRequest.of(0, limit))
                .stream()
                .map(log -> {
                    Map<String,Object> map = new HashMap<>();
                    map.put("wordId", log.getWord().getWordId());
                    map.put("word", log.getWord().getWord());
                    map.put("meaning", log.getWord().getMeaning());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /** 이번 주 요일별 학습 여부 */
    public List<Boolean> getWeeklyStudyStatus() {
        User user = getLoginUser();

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(java.time.DayOfWeek.SUNDAY);

        List<LocalDate> studyDates = studyLogRepository.findStudyDatesBetween(
                user,
                startOfWeek.atStartOfDay(),
                endOfWeek.atTime(23,59,59)
        );

        List<Boolean> week = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = startOfWeek.plusDays(i);
            week.add(studyDates.contains(d));
        }

        return week;
    }
}
