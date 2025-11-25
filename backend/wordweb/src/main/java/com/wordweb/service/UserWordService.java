package com.wordweb.service;

import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.UserWord;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.UserWordRepository;
import com.wordweb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserWordService {

    private final UserWordRepository userWordRepository;
    private final WordRepository wordRepository;
    private final UserRepository userRepository;

    /** 단어 학습 기록 등록 또는 갱신 */
    public void studyWord(String email, Long wordId, String result) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        UserWord userWord = userWordRepository.findByUserAndWord(user, word)
                .orElse(UserWord.builder()
                        .user(user)
                        .word(word)
                        .status("STUDYING")
                        .totalCorrect(0)
                        .totalWrong(0)
                        .build()
                );

        userWord.setLastStudyAt(Timestamp.from(Instant.now()));
        userWord.setLastResult(result);

        if ("CORRECT".equalsIgnoreCase(result)) {
            userWord.setTotalCorrect(userWord.getTotalCorrect() + 1);
        } else {
            userWord.setTotalWrong(userWord.getTotalWrong() + 1);
        }

        userWordRepository.save(userWord);
    }

    /** 학습 기록 조회 */
    public List<UserWord> getMyStudyList(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return userWordRepository.findAllByUser(user);
    }
}
