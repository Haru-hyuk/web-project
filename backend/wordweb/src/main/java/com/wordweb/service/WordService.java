package com.wordweb.service;

import com.wordweb.dto.word.WordResponse;
import com.wordweb.entity.FavoriteWord;
import com.wordweb.entity.StudyLog;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.repository.FavoriteWordRepository;
import com.wordweb.repository.StudyLogRepository;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final FavoriteWordRepository favoriteWordRepository;
    private final StudyLogRepository studyLogRepository;
    private final UserRepository userRepository;

    private final Random random = new Random();

    /** 현재 로그인 유저 조회 */
    private User getLoginUser() {
        try {
            String email = SecurityUtil.getCurrentUserEmail();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /** 즐겨찾기 여부 */
    private boolean isFavorite(User user, Word word) {
        return user != null && favoriteWordRepository.existsByUserAndWord(user, word);
    }

    /** 학습 상태 조회 */
    private String getLearningStatus(User user, Word word) {
        if (user == null) return "NONE";
        return studyLogRepository.findByUserAndWord(user, word)
                .map(log -> log.getStatus())
                .orElse("NONE");
    }

    /** WordResponse 생성 */
    private WordResponse toResponse(User user, Word word) {
        return WordResponse.from(
                word,
                isFavorite(user, word),
                getLearningStatus(user, word)
        );
    }

    /** 단어 상세 조회 */
    public WordResponse getWord(Long id) {
        User user = getLoginUser();

        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        return toResponse(user, word);
    }

    /** 오늘의 랜덤 단어 */
    public WordResponse getTodayWord() {
        User user = getLoginUser();

        List<Word> words = wordRepository.findAll();
        if (words.isEmpty()) throw new RuntimeException("단어 데이터가 없습니다.");

        Word randomWord = words.get(random.nextInt(words.size()));
        return toResponse(user, randomWord);
    }

    /** 전체 단어 리스트 (Pageable) */
    public Page<WordResponse> getWordList(Pageable pageable) {
        User user = getLoginUser();

        return wordRepository.findAll(pageable)
                .map(word -> toResponse(user, word));
    }

    /** 전체 단어 리스트 (모든 단어 한 번에 조회 - 단어장용, N+1 최적화) */
    public List<WordResponse> getAllWords() {
        User user = getLoginUser();

        // 모든 단어 한 번에 조회
        List<Word> allWords = wordRepository.findAll();

        if (user == null) {
            // 로그인하지 않은 경우 즐겨찾기/학습 상태 없이 반환
            return allWords.stream()
                    .map(word -> WordResponse.from(word, false, "NONE"))
                    .toList();
        }

        // 사용자의 모든 즐겨찾기 단어 ID를 한 번에 조회 (N+1 방지, LAZY 로딩 문제 해결)
        List<FavoriteWord> favoriteWords = favoriteWordRepository.findByUserWithWord(user);
        Set<Long> favoriteWordIds = favoriteWords.stream()
                .map(fw -> fw.getWord().getWordId())
                .collect(Collectors.toSet());

        // 사용자의 모든 학습 로그를 한 번에 조회 (N+1 방지, LAZY 로딩 문제 해결)
        List<StudyLog> studyLogs = studyLogRepository.findByUserWithWord(user);
        Map<Long, String> wordStatusMap = studyLogs.stream()
                .collect(Collectors.toMap(
                        log -> log.getWord().getWordId(),
                        StudyLog::getStatus,
                        (existing, replacement) -> existing // 중복 시 기존 값 유지
                ));
        
        Map<Long, LocalDateTime> wordLastStudyMap = studyLogs.stream()
                .collect(Collectors.toMap(
                        log -> log.getWord().getWordId(),
                        StudyLog::getLastStudyAt,
                        (existing, replacement) -> existing != null && (replacement == null || existing.isAfter(replacement)) 
                            ? existing : replacement // 더 최근 날짜 유지
                ));

        // 메모리에서 매칭하여 WordResponse 생성
        return allWords.stream()
                .map(word -> {
                    Long wordId = word.getWordId();
                    boolean isFav = favoriteWordIds.contains(wordId);
                    String status = wordStatusMap.getOrDefault(wordId, "NONE");
                    LocalDateTime lastStudyAt = wordLastStudyMap.get(wordId);
                    return WordResponse.from(word, isFav, status, lastStudyAt);
                })
                .toList();
    }

    /** 검색 (keyword + pageable) */
    public Page<WordResponse> searchWords(String keyword, Pageable pageable) {
        User user = getLoginUser();

        return wordRepository.findByWordContainingIgnoreCase(keyword, pageable)
                .map(word -> toResponse(user, word));
    }

    /** 필터 검색 (category, level, partOfSpeech 조합) */
    public Page<WordResponse> filterWords(
            String category,
            Integer level,
            String partOfSpeech,
            Pageable pageable
    ) {
        User user = getLoginUser();

        Page<Word> result;

        // 아무 필터도 없으면 전체 조회
        if (category == null && level == null && partOfSpeech == null) {
            return getWordList(pageable);
        }

        // 조합 필터
        if (category != null && level != null && partOfSpeech != null) {
            result = wordRepository.findByCategoryAndLevelAndPartOfSpeech(category, level, partOfSpeech, pageable);

        } else if (category != null && level != null) {
            result = wordRepository.findByCategoryAndLevel(category, level, pageable);

        } else if (category != null && partOfSpeech != null) {
            result = wordRepository.findByCategoryAndPartOfSpeech(category, partOfSpeech, pageable);

        } else if (level != null && partOfSpeech != null) {
            result = wordRepository.findByLevelAndPartOfSpeech(level, partOfSpeech, pageable);

        } else if (category != null) {
            result = wordRepository.findByCategory(category, pageable);

        } else if (level != null) {
            result = wordRepository.findByLevel(level, pageable);

        } else {
            result = wordRepository.findByPartOfSpeech(partOfSpeech, pageable);
        }

        return result.map(word -> toResponse(user, word));
    }
}
