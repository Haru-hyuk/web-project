package com.wordweb.service;

import com.wordweb.entity.*;
import com.wordweb.repository.*;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WrongAnswerStoryService {

    private final WrongAnswerStoryRepository wrongAnswerStoryRepository;
    private final WrongAnswerLogRepository wrongAnswerLogRepository;
    private final StoryWordListRepository storyWordListRepository;
    private final UserRepository userRepository;

    /** 로그인 유저 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    /** AI 스토리 생성 후 저장 */
    @Transactional
    public WrongAnswerStory createStory(String title, String storyEn, String storyKo, List<Long> wrongLogIds) {

        User user = getLoginUser();

        // 1) WrongAnswerStory 저장
        WrongAnswerStory story = WrongAnswerStory.create(user, title, storyEn, storyKo);
        wrongAnswerStoryRepository.save(story);

        // 2) StoryWordList 저장 (NEW 구조 대응)
        for (Long wrongLogId : wrongLogIds) {

            WrongAnswerLog wrongLog = wrongAnswerLogRepository.findById(wrongLogId)
                    .orElseThrow(() -> new RuntimeException("오답 로그가 존재하지 않습니다. ID=" + wrongLogId));

            Long wordId = wrongLog.getWord().getWordId();   // 실제 단어의 PK 가져오기

            StoryWordList relation = StoryWordList.create(
                    story.getStoryId(),
                    wordId,
                    wrongLogId
            );

            storyWordListRepository.save(relation);
        }

        // 3) WrongAnswerLog 사용 표시 업데이트
        wrongAnswerLogRepository.findAllById(wrongLogIds)
                .forEach(WrongAnswerLog::markUsedInStory);

        return story;
    }

    /** 스토리 목록 */
    public List<WrongAnswerStory> getMyStories() {
        User user = getLoginUser();
        return wrongAnswerStoryRepository.findByUser(user);
    }

    /** 스토리 상세 조회 */
    public WrongAnswerStory getStoryDetail(Long storyId) {
        return wrongAnswerStoryRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("스토리를 찾을 수 없습니다."));
    }

    /** 스토리에 사용된 오답 목록 조회 */
    public List<StoryWordList> getWrongWordsInStory(Long storyId) {
        return storyWordListRepository.findByStoryId(storyId);
    }
}
