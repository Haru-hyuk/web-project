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
    private final WrongAnswerLogService wrongAnswerLogService;
    private final StudyLogService studyLogService;

    /** 로그인 유저 가져오기 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    /** 단어장 수동 완료: WRONG_ANSWER_LOG 삭제 + COMPLETED_WORD 추가 + STUDY_LOG 상태 변경 */
    @org.springframework.transaction.annotation.Transactional
    public void markCompleted(Long wordId) {
        User user = getLoginUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        if (completedWordRepository.existsByUserAndWord(user, word)) {
            return;
        }

        wrongAnswerLogService.removeWrongAnswer(wordId);
        CompletedWord completed = CompletedWord.create(user, word);
        completedWordRepository.save(completed);
        studyLogService.updateStatusToLearned(wordId);
    }

    /** 완료한 단어 목록 조회 */
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

    /** 단어장 수동 취소: COMPLETED_WORD 삭제 + STUDY_LOG 상태 변경 (WRONG_ANSWER_LOG 추가 안함) */
    @org.springframework.transaction.annotation.Transactional
    public void unmarkCompleted(Long wordId) {
        User user = getLoginUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        completedWordRepository.findByUserAndWord(user, word)
                .ifPresent(completedWordRepository::delete);
        studyLogService.updateStatusToPending(wordId);
    }
}
