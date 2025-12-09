package com.wordweb.service;

import com.wordweb.entity.StoryWordList;
import com.wordweb.entity.User;
import com.wordweb.entity.WrongAnswerLog;
import com.wordweb.entity.WrongAnswerStory;
import com.wordweb.repository.StoryWordListRepository;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WrongAnswerLogRepository;
import com.wordweb.repository.WrongAnswerStoryRepository;
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

    /** 로그인 유저 조회 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    /** AI 스토리 생성 */
    @Transactional
    public WrongAnswerStory createStory(String title, String storyEn, String storyKo, List<Long> wrongLogIds) {

        User user = getLoginUser();

        WrongAnswerStory story = WrongAnswerStory.create(user, title, storyEn, storyKo);
        wrongAnswerStoryRepository.save(story);

        // StoryWordList 저장
        for (Long wrongLogId : wrongLogIds) {
            wrongAnswerLogRepository.findById(wrongLogId).ifPresent(log -> {
                Long wordId = log.getWord().getWordId();
                StoryWordList relation = StoryWordList.create(
                        story.getStoryId(),
                        wordId,
                        wrongLogId
                );
                storyWordListRepository.save(relation);
            });
        }

        // WrongAnswerLog 업데이트
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

    /** 스토리에 사용된 단어 조회 */
    @Transactional(readOnly = true)
    public List<StoryWordList> getWrongWordsInStory(Long storyId) {
        return storyWordListRepository.findByStoryIdWithWord(storyId);
    }

    /** ⭐ 스토리 삭제 */
    @Transactional
    public void deleteStory(Long storyId) {

        User user = getLoginUser();

        WrongAnswerStory story = wrongAnswerStoryRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("스토리를 찾을 수 없습니다."));

        // 본인 스토리인지 검증
        if (!story.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("본인 스토리만 삭제할 수 있습니다.");
        }

        // ⭐ 1) StoryWordList 먼저 삭제 (FK 문제 해결)
        storyWordListRepository.deleteByStoryId(storyId);

        // ⭐ 2) 스토리 삭제
        wrongAnswerStoryRepository.delete(story);
    }
}
