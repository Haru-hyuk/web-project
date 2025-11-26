package com.wordweb.service;

import com.wordweb.dto.word.WordResponse;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.WordProgress;
import com.wordweb.repository.FavoriteWordRepository;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordProgressRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final FavoriteWordRepository favoriteWordRepository;
    private final WordProgressRepository wordProgressRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    /** 현재 로그인 유저 가져오기 */
    private User getLoginUser() {
        try {
            String email = SecurityUtil.getLoginUserEmail();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null; // 비로그인 허용
        }
    }

    /** 즐겨찾기 여부 체크 */
    private boolean isFavorite(User user, Word word) {
        if (user == null) return false;
        return favoriteWordRepository.existsByUserAndWord(user, word);
    }

    /** 학습 상태 조회 */
    private String getLearningStatus(User user, Word word) {
        if (user == null) return "NONE";

        return wordProgressRepository.findByUserAndWord(user, word)
                .map(WordProgress::getStatus)
                .orElse("NONE");
    }

    /** WordResponse 생성 도우미 */
    private WordResponse buildWordResponse(User user, Word word) {
        boolean isFavorite = isFavorite(user, word);
        String learningStatus = getLearningStatus(user, word);

        return WordResponse.from(word, isFavorite, learningStatus);
    }

    /** 단어 1개 조회 */
    public WordResponse getWord(Long id) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        User user = getLoginUser();

        return buildWordResponse(user, word);
    }

    /** 오늘의 단어 랜덤 1개 */
    public WordResponse getTodayWord() {
        List<Word> list = wordRepository.findAll();
        if (list.isEmpty()) throw new RuntimeException("단어 데이터가 없습니다.");

        Word randomWord = list.get(random.nextInt(list.size()));
        User user = getLoginUser();

        return buildWordResponse(user, randomWord);
    }

    /** 전체 단어 조회 */
    public Page<WordResponse> getWordList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word").ascending());
        Page<Word> words = wordRepository.findAll(pageable);

        User user = getLoginUser();

        return words.map(word -> buildWordResponse(user, word));
    }

    /** 검색 */
    public Page<WordResponse> searchWords(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word").ascending());
        Page<Word> words = wordRepository.findByWordContainingIgnoreCase(keyword, pageable);

        User user = getLoginUser();

        return words.map(word -> buildWordResponse(user, word));
    }

    /** 품사 + 검색 */
    public Page<WordResponse> searchWordsWithPart(String keyword, String part, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word").ascending());
        Page<Word> words =
                wordRepository.findByWordContainingIgnoreCaseAndPartOfSpeech(keyword, part, pageable);

        User user = getLoginUser();

        return words.map(word -> buildWordResponse(user, word));
    }

    /** 카테고리 필터 */
    public Page<WordResponse> filterByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word").ascending());
        Page<Word> words = wordRepository.findByCategory(category, pageable);

        User user = getLoginUser();

        return words.map(word -> buildWordResponse(user, word));
    }

    /** 레벨 필터 */
    public Page<WordResponse> filterByLevel(Integer level, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word").ascending());
        Page<Word> words = wordRepository.findByLevel(level, pageable);

        User user = getLoginUser();
        return words.map(word -> buildWordResponse(user, word));
    }


    /** 카테고리 + 레벨 필터 */
    public Page<WordResponse> filterByCategoryAndLevel(String category, Integer level, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word").ascending());
        Page<Word> words = wordRepository.findByCategoryAndLevel(category, level, pageable);

        User user = getLoginUser();

        return words.map(word -> buildWordResponse(user, word));
    }


}