package com.wordweb.service;

import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.WrongWord;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.repository.WrongWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WrongWordService {

    private final WrongWordRepository wrongWordRepository;
    private final WordRepository wordRepository;
    private final UserRepository userRepository;

    /** 오답 추가 */
    public void addWrongWord(String email, Long wordId, String tag) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        if (wrongWordRepository.existsByUserAndWord(user, word)) {
            throw new RuntimeException("이미 오답으로 등록된 단어입니다.");
        }

        WrongWord wrongWord = WrongWord.builder()
                .user(user)
                .word(word)
                .wrongAt(Timestamp.from(Instant.now()))
                .tag(tag)
                .isUsedInStory("N")
                .build();

        wrongWordRepository.save(wrongWord);
    }

    /** 오답 삭제 */
    public void removeWrongWord(String email, Long wordId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        WrongWord wrongWord = wrongWordRepository.findByUserAndWord(user, word)
                .orElseThrow(() -> new RuntimeException("오답으로 등록되지 않은 단어입니다."));

        wrongWordRepository.delete(wrongWord);
    }

    /** 오답 목록 조회 */
    public List<WrongWord> getWrongWordList(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return wrongWordRepository.findAllByUser(user);
    }
}
