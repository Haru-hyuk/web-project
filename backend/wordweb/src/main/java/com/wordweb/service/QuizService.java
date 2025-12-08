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
    private final WrongAnswerLogService wrongAnswerLogService;
    private final CompletedWordService completedWordService;

    /** 로그인 유저 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    /**
     * 퀴즈 생성
     * @param mode normal | wrong
     *   - normal: 일반 퀴즈 (학습 옵션 적용)
     *   - wrong: 오답 다시 풀기 (모든 오답 단어 사용, count 파라미터 무시)
     * @param count 문항 수 (null이면 10)
     *   - normal 모드: 적용됨
     *   - wrong 모드: 무시됨 (모든 오답 단어 사용)
     *   - wordIds 모드: 무시됨 (wordIds 개수만큼 생성)
     * @param level 난이도 (1-6 또는 beginner/intermediate/advanced 또는 null)
     *   - normal 모드: 적용됨
     *   - wrong 모드: 무시됨
     * @param category 카테고리 (null이면 전체)
     *   - normal 모드: 적용됨
     *   - wrong 모드: 무시됨
     * @param wordIds 특정 단어 ID 목록 (null이면 일반 로직)
     *   - 플래시카드 → 퀴즈 플로우에서 사용
     *   - wordIds가 있으면 mode, count, level, category 모두 무시
     */
    public List<QuizQuestionResponse> getQuiz(String mode, Integer count, String level, String category, List<Long> wordIds) {

        User user = getLoginUser();

        List<Word> basePool;

        // 특정 단어 ID로 퀴즈 생성 (플래시카드 → 퀴즈 플로우)
        boolean isWordIdsMode = wordIds != null && !wordIds.isEmpty();
        if (isWordIdsMode) {
            basePool = wordRepository.findAllById(wordIds);
            if (basePool.isEmpty()) {
                throw new RuntimeException("지정한 단어를 찾을 수 없습니다.");
            }
        }
        // 오답 다시 풀기 모드
        else if ("wrong".equalsIgnoreCase(mode)) {
            // FETCH JOIN으로 N+1 문제 해결 (1개의 쿼리로 모든 Word 로딩)
            List<WrongAnswerLog> logs = wrongAnswerLogRepository.findByUserWithWord(user);
            basePool = logs.stream()
                    .map(WrongAnswerLog::getWord)
                    .distinct()
                    .collect(Collectors.toList());
            if (basePool.isEmpty()) {
                throw new RuntimeException("틀린 단어가 없습니다.");
            }
        }
        // 일반 모드 (학습 옵션 적용)
        else {
            basePool = filterWords(level, category);
        }

        int quizCount;
        if (isWordIdsMode) {
            quizCount = basePool.size(); // wordIds 개수만큼 정확히 생성
        } else if ("wrong".equalsIgnoreCase(mode)) {
            quizCount = basePool.size(); // 오답 다시 풀기: 모든 오답 단어 사용
        } else {
            quizCount = count != null ? Math.min(count, basePool.size()) : Math.min(10, basePool.size());
        }
        Collections.shuffle(basePool);

        // 오답 생성을 위해 전체 단어 풀을 미리 조회 (1번의 쿼리로 최적화)
        List<Word> allWords = wordRepository.findAll();

        List<QuizQuestionResponse> result = new ArrayList<>();

        for (int i = 0; i < quizCount; i++) {

            Word qWord = basePool.get(i);

            List<String> options = new ArrayList<>();

            // 정답 meaning
            String correctMeaning = qWord.getMeaning();
            options.add(correctMeaning);

            // 오답 3개 생성 (전체 단어 풀을 재사용)
            List<String> distractors = pickRandomMeanings(qWord, 3, allWords);
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
     * 학습 옵션에 따라 단어 필터링
     */
    private List<Word> filterWords(String level, String category) {
        // 난이도 그룹 처리: beginner(1,2), intermediate(3,4), advanced(5,6)
        List<Integer> levelRange = null;
        if (level != null && !level.isEmpty() && !"all".equalsIgnoreCase(level)) {
            String levelLower = level.toLowerCase();
            if ("beginner".equals(levelLower)) {
                levelRange = Arrays.asList(1, 2);
            } else if ("intermediate".equals(levelLower)) {
                levelRange = Arrays.asList(3, 4);
            } else if ("advanced".equals(levelLower)) {
                levelRange = Arrays.asList(5, 6);
            } else {
                // 숫자로 직접 입력된 경우
                try {
                    Integer levelInt = Integer.parseInt(level);
                    levelRange = Arrays.asList(levelInt);
                } catch (NumberFormatException e) {
                    // 파싱 실패 시 null 유지
                }
            }
        }

        boolean hasCategory = category != null && !category.isEmpty() && !"all".equalsIgnoreCase(category);
        boolean hasLevel = levelRange != null && !levelRange.isEmpty();

        // 필터 조합에 따라 조회 (최적화된 쿼리 사용)
        if (hasCategory && hasLevel) {
            // 카테고리 + 레벨 범위 조회 (1개 쿼리)
            return wordRepository.findByCategoryAndLevelIn(category, levelRange);
        } else if (hasCategory) {
            // 카테고리만 조회
            return wordRepository.findByCategory(category);
        } else if (hasLevel) {
            // 레벨 범위만 조회 (1개 쿼리)
            return wordRepository.findByLevelIn(levelRange);
        } else {
            // 전체 조회
            return wordRepository.findAll();
        }
    }

    /**
     * 오답 생성 로직 (최적화 버전):
     * - 전체 단어 풀을 파라미터로 받아서 재사용 (DB 조회 없음)
     * 1) 같은 품사 + 같은 레벨
     * 2) 같은 품사
     * 3) 전체 단어에서 채우기
     */
    private List<String> pickRandomMeanings(Word correctWord, int count, List<Word> allWords) {

        Set<Word> pool = new LinkedHashSet<>();

        // 같은 품사 + 같은 레벨 (메모리 필터링)
        allWords.stream()
                .filter(w -> w.getPartOfSpeech().equals(correctWord.getPartOfSpeech()))
                .filter(w -> w.getLevel().equals(correctWord.getLevel()))
                .filter(w -> !w.getWordId().equals(correctWord.getWordId()))
                .forEach(pool::add);

        // 부족하면 같은 품사 추가 (메모리 필터링)
        if (pool.size() < count) {
            allWords.stream()
                    .filter(w -> w.getPartOfSpeech().equals(correctWord.getPartOfSpeech()))
                    .filter(w -> !w.getWordId().equals(correctWord.getWordId()))
                    .forEach(pool::add);
        }

        // 그래도 부족하면 전체 단어에서 채우기
        if (pool.size() < count) {
            allWords.stream()
                    .filter(w -> !w.getWordId().equals(correctWord.getWordId()))
                    .forEach(pool::add);
        }

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
     * 
     * 실전 퀴즈 풀기 (mode=normal):
     * - 처음 푼 모든 영단어: STUDY_LOG 생성/업데이트
     * - 정답: STUDY_LOG 정답 횟수 증가 + COMPLETED_WORD 저장 + WRONG_ANSWER_LOG 삭제 (있으면)
     * - 오답: STUDY_LOG 오답 횟수 증가 + WRONG_ANSWER_LOG 저장 + COMPLETED_WORD 삭제 (있으면)
     * 
     * 오답 다시 풀기 (mode=wrong):
     * - 정답: WRONG_ANSWER_LOG 삭제 + COMPLETED_WORD 저장 + STUDY_LOG 정답 횟수 증가
     * - 오답: WRONG_ANSWER_LOG 유지 + IS_USED_IN_STORY가 true면 false로 변경 + STUDY_LOG 오답 횟수 증가
     */
    @org.springframework.transaction.annotation.Transactional
    public void saveResult(QuizResultRequest request) {
        // 요청 데이터 검증
        if (request == null) {
            throw new IllegalArgumentException("퀴즈 결과 요청이 null입니다.");
        }
        
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("답안 목록이 비어있습니다.");
        }
        
        String mode = request.getMode() != null ? request.getMode() : "normal";
        boolean isWrongMode = "wrong".equalsIgnoreCase(mode);

        int validAnswerCount = 0;
        for (QuizResultRequest.Answer ans : request.getAnswers()) {
            if (ans == null) {
                continue; // 답안이 null이면 스킵
            }
            
            Long wordId = ans.getWordId();
            if (wordId == null) {
                continue; // wordId가 null이면 스킵
            }
            
            validAnswerCount++;
            
            boolean isCorrect = ans.isCorrect();

            if (isCorrect) {
                // 정답 처리
                studyLogService.markCorrect(wordId);
                completedWordService.markCompleted(wordId);
                
                // 정답을 맞춘 단어가 WRONG_ANSWER_LOG에 있으면 삭제
                wrongAnswerLogService.removeWrongAnswer(wordId);
            } else {
                // 오답 처리
                studyLogService.markWrong(wordId);

                if (isWrongMode) {
                    // 오답 다시 풀기 모드: 틀리면 IS_USED_IN_STORY가 true면 false로 변경
                    User user = getLoginUser();
                    Word word = wordRepository.findById(wordId)
                            .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));
                    
                    wrongAnswerLogRepository.findByUserAndWord(user, word)
                            .ifPresent(log -> {
                                if (Boolean.TRUE.equals(log.getIsUsedInStory())) {
                                    log.setIsUsedInStory(false);
                                    wrongAnswerLogRepository.save(log);
                                }
                            });
                } else {
                    // 실전 퀴즈 풀기 모드: 오답이면 WRONG_ANSWER_LOG 저장
                    try {
                        wrongAnswerLogService.addWrongAnswer(wordId);
                    } catch (Exception e) {
                        // 오답 기록 저장 실패 시 무시
                    }
                    
                    // COMPLETED_WORD에 있으면 삭제 (완료된 단어를 틀렸으므로 완료 상태 취소)
                    try {
                        if (completedWordService.isCompleted(wordId)) {
                            completedWordService.unmarkCompleted(wordId);
                        }
                    } catch (Exception e) {
                        // 완료 기록 삭제 실패 시 무시
                    }
                }
            }
        }
        
        if (validAnswerCount == 0) {
            throw new IllegalArgumentException("유효한 답안이 없습니다. 모든 답안의 wordId가 null이거나 유효하지 않습니다.");
        }
    }
}
