package com.wordweb.service;

import com.wordweb.dto.learn.QuizQuestionResponse;
import com.wordweb.dto.learn.QuizResultRequest;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.WrongAnswerLog;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.repository.WrongAnswerLogRepository;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final WordRepository wordRepository;
    private final WrongAnswerLogRepository wrongAnswerLogRepository;
    private final UserRepository userRepository;
    private final StudyLogService studyLogService;

    private final Random random = new Random();

    /** 로그인 유저 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    /**
     * 퀴즈 생성: mode = normal | wrong
     */
    public List<QuizQuestionResponse> getQuiz(String mode) {

        User user = getLoginUser();

        List<Word> basePool;

        if ("wrong".equalsIgnoreCase(mode)) {
            List<WrongAnswerLog> logs = wrongAnswerLogRepository.findByUser(user);

            basePool = logs.stream()
                    .map(WrongAnswerLog::getWord)
                    .collect(Collectors.toList());

            if (basePool.isEmpty()) {
                throw new RuntimeException("오답 기록이 없습니다. normal 모드로 시도하세요.");
            }

        } else {
            basePool = wordRepository.findAll();
        }

        if (basePool.size() < 4) {
            throw new RuntimeException("퀴즈를 생성하기에 단어가 부족합니다. 최소 4개 필요합니다.");
        }

        // 문제 수: 기본 10개
        int quizCount = Math.min(10, basePool.size());
        Collections.shuffle(basePool);

        List<QuizQuestionResponse> result = new ArrayList<>();

        for (int i = 0; i < quizCount; i++) {

            Word qWord = basePool.get(i);

            List<String> options = new ArrayList<>();

            // 정답 meaning
            String correctMeaning = qWord.getMeaning();
            options.add(correctMeaning);

            // 오답 3개 생성
            List<String> distractors = pickRandomMeanings(qWord, 3);
            options.addAll(distractors);

            Collections.shuffle(options);
            int answerIndex = options.indexOf(correctMeaning);

            result.add(
                    QuizQuestionResponse.builder()
                            .wordId(qWord.getWordId())
                            .word(qWord.getWord())
                            .options(options)
                            .answerIndex(answerIndex)
                            .build()
            );
        }

        return result;
    }

    /**
     * 오답 생성 로직:
     * 1) 같은 품사 + 같은 레벨
     * 2) 같은 품사
     * 3) 전체 단어에서 랜덤
     */
    private List<String> pickRandomMeanings(Word correctWord, int count) {

        // 1️⃣ 같은 품사 + 같은 레벨
        List<Word> pool = wordRepository
                .findByPartOfSpeechAndLevel(
                        correctWord.getPartOfSpeech(),
                        correctWord.getLevel()
                )
                .stream()
                .filter(w -> !w.getWordId().equals(correctWord.getWordId()))
                .collect(Collectors.toList());

        // 2️⃣ 부족하면 같은 품사 채우기
        if (pool.size() < count) {
            List<Word> samePos = wordRepository
                    .findByPartOfSpeech(correctWord.getPartOfSpeech())
                    .stream()
                    .filter(w -> !w.getWordId().equals(correctWord.getWordId()))
                    .collect(Collectors.toList());

            for (Word w : samePos) {
                if (!pool.contains(w)) {
                    pool.add(w);
                }
            }
        }

        // 3️⃣ 그래도 부족하면 전체에서 채우기
        if (pool.size() < count) {
            List<Word> all = wordRepository.findAll()
                    .stream()
                    .filter(w -> !w.getWordId().equals(correctWord.getWordId()))
                    .collect(Collectors.toList());

            for (Word w : all) {
                if (!pool.contains(w)) {
                    pool.add(w);
                }
            }
        }

        // 🔥 최종 pool이 여전히 부족하면 오류
        if (pool.size() < count) {
            throw new RuntimeExc



    /**
     * 퀴즈 결과 저장
     */
    public void saveResult(QuizResultRequest request) {

        for (QuizResultRequest.Answer ans : request.getAnswers()) {

            if (ans.isCorrect()) {
                studyLogService.markCorrect(ans.getWordId());
            } else {
                studyLogService.markWrong(ans.getWordId());
            }
        }
    }
}
