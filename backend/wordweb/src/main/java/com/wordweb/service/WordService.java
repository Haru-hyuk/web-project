package com.wordweb.service;

import com.wordweb.dto.word.WordResponse;
import com.wordweb.entity.Word;
import com.wordweb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final Random random = new Random();

    /** 단어 1개 조회 */
    public WordResponse getWord(Long id) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));
        return WordResponse.from(word);
    }

    /** 오늘의 단어 (랜덤 1개) */
    public WordResponse getTodayWord() {
        List<Word> list = wordRepository.findAll();
        if (list.isEmpty()) throw new RuntimeException("단어 데이터가 없습니다.");

        Word randomWord = list.get(random.nextInt(list.size()));
        return WordResponse.from(randomWord);
    }

    /** 전체 단어 조회 (페이징) */
    public Page<WordResponse> getWordList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word").ascending());
        Page<Word> result = wordRepository.findAll(pageable);

        return result.map(WordResponse::from);
    }

    /** 키워드 검색 */
    public Page<WordResponse> searchWords(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word").ascending());
        Page<Word> result = wordRepository.findByWordContainingIgnoreCase(keyword, pageable);

        return result.map(WordResponse::from);
    }

    /** 품사 + 키워드 검색 (옵션) */
    public Page<WordResponse> searchWordsWithPart(String keyword, String part, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("word").ascending());

        Page<Word> result =
                wordRepository.findByWordContainingIgnoreCaseAndPartOfSpeech(keyword, part, pageable);

        return result.map(WordResponse::from);
    }
}
