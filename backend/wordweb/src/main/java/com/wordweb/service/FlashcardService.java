package com.wordweb.service;

import com.wordweb.dto.word.WordResponse;
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
public class FlashcardService {

    private final WordRepository wordRepository;
    private final WrongAnswerLogRepository wrongAnswerLogRepository;
    private final UserRepository userRepository;

    /**
     * 플래시카드용 단어 목록 가져오기
     * - 퀴즈와 동일한 필터링 로직 사용
     * - 랜덤하게 섞어서 반환
     */
    public List<WordResponse> getFlashcardWords(Integer count, String level, String category) {

        List<Word> basePool;

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
                // 숫자로 직접 입력된 경우 (기존 호환성)
                try {
                    Integer levelInt = Integer.parseInt(level);
                    levelRange = Arrays.asList(levelInt);
                } catch (NumberFormatException e) {
                    // 파싱 실패 시 null 유지
                    levelRange = null;
                }
            }
        }

        boolean hasCategory = category != null && !category.isEmpty() && !"all".equalsIgnoreCase(category);
        boolean hasLevel = levelRange != null && !levelRange.isEmpty();

        // 카테고리 + 난이도 조합에 따라 필터링
        if (hasCategory && hasLevel) {
            // 카테고리와 난이도 범위 모두 적용
            final String finalCategory = category;
            final List<Integer> finalLevelRange = levelRange;
            basePool = wordRepository.findAll().stream()
                    .filter(w -> finalCategory.equals(w.getCategory()))
                    .filter(w -> finalLevelRange.contains(w.getLevel()))
                    .collect(Collectors.toList());
        } else if (hasCategory) {
            basePool = wordRepository.findByCategory(category);
        } else if (hasLevel) {
            // 난이도 범위 필터링
            final List<Integer> finalLevelRange = levelRange;
            basePool = wordRepository.findAll().stream()
                    .filter(w -> finalLevelRange.contains(w.getLevel()))
                    .collect(Collectors.toList());
        } else {
            basePool = wordRepository.findAll();
        }

        if (basePool.isEmpty()) {
            throw new RuntimeException("조건에 맞는 단어가 없습니다.");
        }

        // 랜덤하게 섞기
        Collections.shuffle(basePool);

        // count 개수만큼 선택 (요청한 개수와 실제 단어 개수 중 작은 값)
        // 문항수 미만이더라도 가져온 단어 수만큼 플래시카드 생성
        int actualCount = Math.min(count != null ? count : 10, basePool.size());
        List<Word> selectedWords = basePool.subList(0, actualCount);

        // WordResponse로 변환
        return selectedWords.stream()
                .map(this::toWordResponse)
                .collect(Collectors.toList());
    }

    /**
     * 오답 플래시카드용 단어 목록 가져오기
     * - WRONG_ANSWER_LOG에 있는 모든 단어들을 대상으로 함
     * - 일반 플래시카드와 동일한 기능이지만 단어 소스만 다름
     * - count 파라미터 무시: 모든 오답 단어 반환
     * @param count 무시됨 (모든 오답 단어 반환)
     */
    public List<WordResponse> getWrongAnswerFlashcardWords(Integer count) {
        User user = getLoginUser();

        // 오답 기록에서 단어 추출
        List<WrongAnswerLog> wrongLogs = wrongAnswerLogRepository.findByUser(user);

        if (wrongLogs.isEmpty()) {
            throw new RuntimeException("틀린 단어가 없습니다.");
        }

        // 중복 제거하고 Word 객체만 추출
        List<Word> wrongWords = wrongLogs.stream()
                .map(WrongAnswerLog::getWord)
                .distinct()
                .collect(Collectors.toList());

        // 랜덤하게 섞기
        Collections.shuffle(wrongWords);

        // count 파라미터 무시: 모든 오답 단어 반환
        // WRONG_ANSWER_LOG 테이블에 있는 모든 틀린 단어들로 플래시카드 생성
        List<Word> selectedWords = wrongWords;

        // WordResponse로 변환
        return selectedWords.stream()
                .map(this::toWordResponse)
                .collect(Collectors.toList());
    }

    /** 로그인 유저 가져오기 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    private WordResponse toWordResponse(Word word) {
        return WordResponse.builder()
                .wordId(word.getWordId())
                .word(word.getWord())
                .meaning(word.getMeaning())
                .partOfSpeech(word.getPartOfSpeech())
                .level(word.getLevel())
                .category(word.getCategory())
                .exampleSentenceEn(word.getExampleSentenceEn())
                .exampleSentenceKo(word.getExampleSentenceKo())
                .favorite(false)  // 플래시카드에서는 기본값 false
                .learningStatus(null)  // 플래시카드에서는 학습 상태 정보 없음
                .build();
    }
}
