package com.wordweb.service;

import com.wordweb.dto.wrong.WrongAnswerLogResponse;
import com.wordweb.entity.StudyLog;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.entity.WrongAnswerLog;
import com.wordweb.repository.StudyLogRepository;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.repository.WrongAnswerLogRepository;
import com.wordweb.repository.StoryWordListRepository;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WrongAnswerLogService {

    private final WrongAnswerLogRepository wrongAnswerLogRepository;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final StoryWordListRepository storyWordListRepository;
    private final StudyLogRepository studyLogRepository;

    /** 로그인 유저 가져오기 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    /** 오답 기록 추가 */
    @org.springframework.transaction.annotation.Transactional
    public void addWrongAnswer(Long wordId) {
        User user = getLoginUser();

        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        // 이미 존재하는 오답 기록이 있는지 확인
        Optional<WrongAnswerLog> existingLog = wrongAnswerLogRepository.findByUserAndWord(user, word);

        if (existingLog.isEmpty()) {
            // 존재하지 않으면 새로 생성
            WrongAnswerLog log = WrongAnswerLog.create(user, word);
            wrongAnswerLogRepository.save(log);
        }
    }

    /** 오답 기록 추가 (wrongWordId 반환) */
    @org.springframework.transaction.annotation.Transactional
    public Long addWrongAnswerAndReturnId(Long wordId) {
        User user = getLoginUser();

        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        // 이미 존재하는 오답 기록이 있는지 확인
        Optional<WrongAnswerLog> existingLog = wrongAnswerLogRepository.findByUserAndWord(user, word);

        if (existingLog.isPresent()) {
            // 이미 존재하면 기존 wrongWordId 반환
            return existingLog.get().getWrongWordId();
        } else {
            // 존재하지 않으면 새로 생성
            WrongAnswerLog log = WrongAnswerLog.create(user, word);
            WrongAnswerLog saved = wrongAnswerLogRepository.save(log);
            return saved.getWrongWordId();
        }
    }

    /** 유저의 오답 기록 조회 (Word 엔티티 함께 로딩 - LAZY 로딩 문제 해결) */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<WrongAnswerLog> getMyWrongLogs() {
        User user = getLoginUser();
        // FETCH JOIN을 사용하여 Word 엔티티를 함께 로딩
        return wrongAnswerLogRepository.findByUserWithWord(user);
    }

    /** 유저의 오답 기록 조회 (DTO 반환 - totalWrong 포함) */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<WrongAnswerLogResponse> getMyWrongLogsWithTotalWrong() {
        User user = getLoginUser();
        List<WrongAnswerLog> wrongLogs = wrongAnswerLogRepository.findByUserWithWord(user);
        
        return wrongLogs.stream().map(log -> {
            Word word = log.getWord();
            
            // StudyLog에서 totalWrong 조회
            Integer totalWrong = 0;
            Optional<StudyLog> studyLogOpt = studyLogRepository.findByUserAndWord(user, word);
            if (studyLogOpt.isPresent()) {
                totalWrong = studyLogOpt.get().getTotalWrong() != null ? studyLogOpt.get().getTotalWrong() : 0;
            }
            
            return WrongAnswerLogResponse.builder()
                    .wrongWordId(log.getWrongWordId())
                    .wordId(word.getWordId())
                    .word(word.getWord())
                    .meaning(word.getMeaning())
                    .level(word.getLevel())
                    .wrongAt(log.getWrongAt())
                    .isUsedInStory(log.getIsUsedInStory())
                    .totalWrong(totalWrong)
                    .build();
        }).collect(Collectors.toList());
    }

    /** 스토리에 아직 사용되지 않은 오답 조회 (Word 엔티티 함께 로딩) */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<WrongAnswerLog> getUnusedWrongLogs() {
        User user = getLoginUser();
        // FETCH JOIN을 사용하여 Word 엔티티를 함께 로딩 (LAZY 로딩 문제 해결)
        return wrongAnswerLogRepository.findByUserAndIsUsedInStoryFalseWithWord(user);
    }

    /** 오답 로그 → 스토리 사용(Y)로 변경 */
    public void markUsedInStory(Long wrongLogId) {
        WrongAnswerLog log = wrongAnswerLogRepository.findById(wrongLogId)
                .orElseThrow(() -> new RuntimeException("오답 기록을 찾을 수 없습니다."));

        log.markUsedInStory();
        wrongAnswerLogRepository.save(log);
    }
    /** 오답 해결 (삭제) */
    @org.springframework.transaction.annotation.Transactional
    public void removeWrongAnswer(Long wordId) {
        User user = getLoginUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다: wordId=" + wordId));

        // 오답 기록 단일 조회 (없으면 그냥 리턴 - idempotent)
        Optional<WrongAnswerLog> logOpt = wrongAnswerLogRepository.findByUserAndWord(user, word);
        
        if (logOpt.isPresent()) {
            WrongAnswerLog log = logOpt.get();
            Long wrongWordId = log.getWrongWordId();
            
            // STORY_WORD_LIST에서 해당 Wrong_Word_ID를 NULL로 업데이트
            // 히스토리는 유지 (Story_ID, Word_ID는 그대로)
            // 예외가 발생해도 WRONG_ANSWER_LOG 삭제는 진행
            try {
                // 직접 UPDATE 쿼리 사용 (Hibernate 영속성 문제 회피)
                // clearAutomatically=true로 영속성 컨텍스트 자동 클리어
                storyWordListRepository.updateWrongWordIdToNull(wrongWordId);
            } catch (Exception e) {
                // STORY_WORD_LIST 업데이트 실패해도 WRONG_ANSWER_LOG 삭제는 진행
                // (히스토리 업데이트는 선택적이므로)
            }
            
            // Wrong_Answer_Log 삭제 (STORY_WORD_LIST 업데이트 후)
            wrongAnswerLogRepository.delete(log);
            wrongAnswerLogRepository.flush();  // 즉시 DB에 반영
        }
    }

}
