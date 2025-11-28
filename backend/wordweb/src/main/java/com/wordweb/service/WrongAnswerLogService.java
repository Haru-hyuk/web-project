package com.wordweb.service;

import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.WrongAnswerLog;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.repository.WrongAnswerLogRepository;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WrongAnswerLogService {

    private final WrongAnswerLogRepository wrongAnswerLogRepository;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;

    /** 로그인 유저 가져오기 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    /** 오답 기록 추가 */
    public void addWrongAnswer(Long wordId) {
        User user = getLoginUser();

        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        WrongAnswerLog log = WrongAnswerLog.create(user, word);
        wrongAnswerLogRepository.save(log);
    }

    /** 유저의 전체 오답 기록 조회 */
    public List<WrongAnswerLog> getMyWrongLogs() {
        User user = getLoginUser();
        return wrongAnswerLogRepository.findByUser(user);
    }

    /** 스토리에 아직 사용되지 않은 오답 조회 */
    public List<WrongAnswerLog> getUnusedWrongLogs() {
        User user = getLoginUser();
        return wrongAnswerLogRepository.findByUserAndIsUsedInStory(user, false);
    }

    /** 오답 로그 → 스토리 사용(Y)로 변경 */
    public void markUsedInStory(Long wrongLogId) {
        WrongAnswerLog log = wrongAnswerLogRepository.findById(wrongLogId)
                .orElseThrow(() -> new RuntimeException("오답 기록을 찾을 수 없습니다."));

        log.markUsedInStory();
        wrongAnswerLogRepository.save(log);
    }
    /** 오답 삭제 */
    public void removeWrongAnswer(Long wordId) {
        User user = getLoginUser();

        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        // 오답 기록 단일 조회
        WrongAnswerLog log = wrongAnswerLogRepository.findByUserAndWord(user, word)
                .orElseThrow(() -> new RuntimeException("해당 오답 기록을 찾을 수 없습니다."));

        wrongAnswerLogRepository.delete(log);
    }

}
