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
     * 3) 전체 단어에서 채우기
     */
    private List<String> pickRandomMeanings(Word correctWord, int count) {

        Set<Word> pool = new LinkedHashSet<>();

        // 1️⃣ 같은 품사 + 같은 레벨
        List<Word> samePosLevel = wordRepository
                .findByPartOfSpeechAndLevel(
                        correctWord.getPartOfSpeech(),
                        correctWord.getLevel()
                );
        samePosLevel.forEach(pool::add);

        // 자기 자신 제거
        pool.remove(correctWord);

        // 2️⃣ 부족하면 같은 품사 추가
        if (pool.size() < count) {
            List<Word> samePos = wordRepository
                    .findByPartOfSpeech(correctWord.getPartOfSpeech());
            samePos.forEach(pool::add);
        }

        // 자기 자신 제거(2번째 안전 제거)
        pool.remove(correctWord);

        // 3️⃣ 그래도 부족하면 전체 단어에서 채우기
        if (pool.size() < count) {
            List<Word> all = wordRepository.findAll();
            all.forEach(pool::add);
        }

        // 자기 자신 제거(3번째 안전 제거)
        pool.remove(correctWord);

        // 🔥 최종적으로도 부족하면 예외 (거의 안일어남)
        if (pool.size() < count) {
            throw new RuntimeException("오답 선택지를 생성하기에 단어가 부족합니다.");
        }

        // 랜덤 meaning N개 추출
        List<Word> finalList = new ArrayList<>(pool);
        Collections.shuffle(finalList);

        return finalList.stream()
                .limit(count)
                .map(Word::getMeaning)
                .toList();
    }

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
