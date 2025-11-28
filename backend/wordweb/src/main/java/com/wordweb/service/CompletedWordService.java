package com.wordweb.service;

import com.wordweb.entity.CompletedWord;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.repository.CompletedWordRepository;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompletedWordService {

    private final CompletedWordRepository completedWordRepository;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;

    /** 로그인 유저 가져오기 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    /** 단어 학습 완료 처리 */
    public void markCompleted(Long wordId) {
        User user = getLoginUser();

        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        // 이미 완료된 단어라면 아무것도 하지 않음 (idempotent)
        if (completedWordRepository.existsByUserAndWord(user, word)) {
            return;
        }

        completedWordRepository.save(CompletedWord.create(user, word));
    }

    /** 학습 완료 단어 전체 조회 */
    public List<CompletedWord> getMyCompletedWords() {
        User user = getLoginUser();
        return completedWordRepository.findByUser(user);
    }

    /** 특정 단어를 완료했는지 여부 */
    public boolean isCompleted(Long wordId) {
        User user = getLoginUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));
        return completedWordRepository.existsByUserAndWord(user, word);
    }
}
