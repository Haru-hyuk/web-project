package com.wordweb.service;

import com.wordweb.dto.progress.StudyLogResponse;
import com.wordweb.entity.StudyLog;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.repository.StudyLogRepository;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyLogService {

    private final StudyLogRepository studyLogRepository;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;

    /** 현재 로그인 유저 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    @Transactional
    public StudyLogResponse markCorrect(Long wordId) {
        User user = getLoginUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        StudyLog log = studyLogRepository.findByUserAndWord(user, word)
                .orElseGet(() -> StudyLog.create(user, word));

        log.markCorrect();
        studyLogRepository.save(log);

        return StudyLogResponse.from(log);
    }

    @Transactional
    public StudyLogResponse markWrong(Long wordId) {
        User user = getLoginUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        StudyLog log = studyLogRepository.findByUserAndWord(user, word)
                .orElseGet(() -> StudyLog.create(user, word));

        log.markWrong();
        studyLogRepository.save(log);

        return StudyLogResponse.from(log);
    }

    public String getStatus(Long wordId) {
        User user = getLoginUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        return studyLogRepository.findByUserAndWord(user, word)
                .map(StudyLog::getStatus)
                .orElse("pending");
    }

}
