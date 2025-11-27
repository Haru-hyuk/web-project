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

    private User getLoginUser() {
        try {
            String email = SecurityUtil.getLoginUserEmail();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isFavorite(User user, Word word) {
        return user != null && favoriteWordRepository.existsByUserAndWord(user, word);
    }

    private String getLearningStatus(User user, Word word) {
        if (user == null) return "NONE";
        return wordProgressRepository.findByUserAndWord(user, word)
                .map(WordProgress::getStatus)
                .orElse("NONE");
    }

    private WordResponse buildWordResponse(User user, Word word) {
        return WordResponse.from(
                word,
                isFavorite(user, word),
                getLearningStatus(user, word)
        );
    }

    public WordResponse getWord(Long id) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));
        return buildWordResponse(getLoginUser(), word);
    }

    public WordResponse getTodayWord() {
        List<Word> list = wordRepository.findAll();
        if (list.isEmpty()) throw new RuntimeException("단어 데이터가 없습니다.");

        Word randomWord = list.get(random.nextInt(list.size()));
        return buildWordResponse(getLoginUser(), randomWord);
    }

    public Page<WordResponse> getWordList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word"));
        return wordRepository.findAll(pageable)
                .map(word -> buildWordResponse(getLoginUser(), word));
    }

    public Page<WordResponse> searchWords(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word"));
        return wordRepository.findByWordContainingIgnoreCase(keyword, pageable)
                .map(word -> buildWordResponse(getLoginUser(), word));
    }

    public Page<WordResponse> searchWordsWithPart(String keyword, String part, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word"));
        return wordRepository.findByWordContainingIgnoreCaseAndPartOfSpeech(keyword, part, pageable)
                .map(word -> buildWordResponse(getLoginUser(), word));
    }

    public Page<WordResponse> filterByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word"));
        return wordRepository.findByCategory(category, pageable)
                .map(word -> buildWordResponse(getLoginUser(), word));
    }

 // ✔ level → wordLevel 로 수정
    public Page<WordResponse> filterByLevel(Integer level, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word"));
        return wordRepository.findByWordLevel(level, pageable)
                .map(word -> buildWordResponse(getLoginUser(), word));
    }

    // ✔ level → wordLevel 로 수정
    public Page<WordResponse> filterByCategoryAndLevel(String category, Integer level, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word"));
        return wordRepository.findByCategoryAndWordLevel(category, level, pageable)
                .map(word -> buildWordResponse(getLoginUser(), word));
    }

}
