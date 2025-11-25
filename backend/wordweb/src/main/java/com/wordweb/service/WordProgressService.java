package com.wordweb.service;

import com.wordweb.dto.word.WordResponse;
import com.wordweb.dto.progress.ProgressUpdateRequest;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.WordProgress;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordProgressRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WordProgressService {

    private final WordProgressRepository progressRepository;
    private final WordRepository wordRepository;
    private final UserRepository userRepository;

    /** 로그인 유저 조회 */
    private User getLoginUser() {
        String email = SecurityUtil.getLoginUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    /** 단어 학습 상태 업데이트 (IN_PROGRESS / DONE) */
    @Transactional
    public void updateProgress(Long wordId, ProgressUpdateRequest request) {

        User user = getLoginUser();

        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        // 기존 기록 여부 확인
        WordProgress progress = progressRepository.findByUserAndWord(user, word)
                .orElse(WordProgress.builder()
                        .user(user)
                        .word(word)
                        .build()
                );

        // 상태 업데이트
        progress.setStatus(request.getStatus());
        progress.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        progressRepository.save(progress);
    }

    /** 단어별 학습 상태 조회 */
    public String getLearningStatus(Long wordId) {

        User user = getLoginUser();

        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        return progressRepository.findByUserAndWord(user, word)
                .map(WordProgress::getStatus)
                .orElse("NONE");  // 학습 기록 없음
    }

    /** 오늘 학습한 단어 수 조회 */
    public int getTodayCount() {

        User user = getLoginUser();
        List<WordProgress> logs = progressRepository.findByUser(user);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return (int) logs.stream()
                .filter(p -> p.getUpdatedAt().toLocalDateTime().toLocalDate().isEqual(today))
                .count();
    }

    /** 최근 7일 학습 통계 */
    public int getWeekCount() {

        User user = getLoginUser();
        List<WordProgress> logs = progressRepository.findByUser(user);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return (int) logs.stream()
                .filter(p -> {
                    LocalDate date = p.getUpdatedAt().toLocalDateTime().toLocalDate();
                    return !date.isBefore(today.minusDays(6)) && !date.isAfter(today);
                })
                .count();
    }

    /** 최근 30일 학습 통계 */
    public int getMonthCount() {

        User user = getLoginUser();
        List<WordProgress> logs = progressRepository.findByUser(user);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return (int) logs.stream()
                .filter(p -> {
                    LocalDate date = p.getUpdatedAt().toLocalDateTime().toLocalDate();
                    return !date.isBefore(today.minusDays(29)) && !date.isAfter(today);
                })
                .count();
    }
}
