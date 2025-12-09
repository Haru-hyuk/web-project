package com.wordweb.service;

import com.wordweb.entity.User;
import com.wordweb.repository.*;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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

    /** 현재 로그인 유저 조회 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    /** 1) 오늘 목표 API */
    public Map<String, Object> getDailyGoal() {
        User user = getLoginUser();

        int goal = user.getDailyWordGoal();  
        int completedToday = completedWordRepository.countTodayCompleted(user.getUserId());
        int progressRate = (int) ((completedToday / (double) goal) * 100);

        Map<String, Object> result = new HashMap<>();
        result.put("nickname", user.getNickname());
        result.put("dailyGoal", goal);
        result.put("completedToday", completedToday);
        result.put("todayProgress", completedToday); 
        result.put("progressRate", progressRate);
        result.put("percentage", progressRate); 
        return result;
    }

    /** 2) 전체 통계 API */
    public Map<String, Object> getStats() {
        User user = getLoginUser();

        long totalWords = wordRepository.count();
        long favorites = favoriteWordRepository.countByUser(user);
        long completed = completedWordRepository.countByUser(user);
        long wrongAnswers = wrongAnswerLogRepository.countByUser(user);
        int streak = getStreak();

        Map<String, Object> result = new HashMap<>();
        result.put("totalWords", totalWords);
        result.put("favoriteWords", favorites);
        result.put("completedWords", completed);
        result.put("wrongAnswers", wrongAnswers);
        result.put("totalLearnedWords", completed); // 프론트엔드 호환성
        result.put("streakDays", streak);
        return result;
    }

    /** 3) 최근 7일 학습량 API */
    public List<Map<String, Object>> getWeeklyStats() {
        User user = getLoginUser();

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate target = today.minusDays(i);

            int learnedCount = studyLogRepository.countLearnedByUserAndDate(user.getUserId(), target);
            int wrongCount = studyLogRepository.sumWrongByUserAndDate(user.getUserId(), target);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", target.toString());
            dayData.put("learnedCount", learnedCount);
            dayData.put("wrongCount", wrongCount);
            result.add(dayData);
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
            int count = studyLogRepository.countByUserAndDate(userId, target);

            if (count > 0) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    /** 오답 Top 5 - study_log 테이블 기반 */
    public List<Map<String,Object>> getWrongTop5() {
        User user = getLoginUser();

        // study_log 테이블에서 totalWrong 기준으로 top5 조회
        List<Object[]> results = studyLogRepository.findTop5ByTotalWrong(user.getUserId());

        return results.stream()
                .map(row -> {
                    Map<String,Object> map = new HashMap<>();
                    map.put("wordId", row[0]);        // wordId
                    map.put("word", row[1]);          // word
                    map.put("meaning", row[2]);       // meaning
                    map.put("count", row[3]);         // wrongCount
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

    /** 이번 주 요일별 학습 여부 - 일요일부터 시작 */
    public List<Boolean> getWeeklyStudyStatus() {
        User user = getLoginUser();

        LocalDate today = LocalDate.now();
        // 이번 주 일요일 구하기 (오늘이 일요일이면 오늘, 아니면 이전 일요일)
        int dayOfWeek = today.getDayOfWeek().getValue(); // Monday=1, Sunday=7
        int daysFromSunday = (dayOfWeek == 7) ? 0 : dayOfWeek; // Sunday=0, Monday=1, ..., Saturday=6
        LocalDate startOfWeek = today.minusDays(daysFromSunday);
        // 이번 주 토요일까지
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<LocalDate> studyDates = studyLogRepository.findStudyDatesBetween(
                user.getUserId(),
                startOfWeek.atStartOfDay(),
                endOfWeek.atTime(23,59,59)
        );

        List<Boolean> week = new ArrayList<>();
        // 일요일(0)부터 토요일(6)까지
        for (int i = 0; i < 7; i++) {
            LocalDate d = startOfWeek.plusDays(i);
            week.add(studyDates.contains(d));
        }

        return week;
    }
}
