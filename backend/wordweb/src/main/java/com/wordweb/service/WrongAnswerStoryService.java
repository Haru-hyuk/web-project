package com.wordweb.service;

import com.wordweb.dto.story.StoryListResponse;
import com.wordweb.entity.*;
import com.wordweb.repository.*;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    /** AI 스토리 생성 후 저장 (최종 엔티티 생성) */
    @Transactional
    public WrongAnswerStory createStory(String title, String titleKo, String storyEn, String storyKo, List<Long> wrongLogIds) {

        User user = getLoginUser();

        // 1) WrongAnswerStory 저장
        WrongAnswerStory story = WrongAnswerStory.create(user, title, titleKo, storyEn, storyKo);
        wrongAnswerStoryRepository.save(story);

        // 2) StoryWordList 연관 엔티티 저장 (WORD_ID + WRONG_WORD_ID 둘 다 저장)
        for (Long wrongLogId : wrongLogIds) {
            wrongAnswerLogRepository.findById(wrongLogId)
                    .ifPresent(log -> {
                        Long wordId = log.getWord().getWordId();  // WORD_ID 추출
                        StoryWordList relation = StoryWordList.create(
                                story.getStoryId(),
                                wordId,        // WORD_ID (히스토리 보존)
                                wrongLogId     // WRONG_WORD_ID (오답 추적)
                        );
                        storyWordListRepository.save(relation);
                    });
        }

        // 3) WrongAnswerLog = isUsedInStory = true 업데이트
        wrongAnswerLogRepository.findAllById(wrongLogIds)
                .forEach(log -> log.markUsedInStory());

        return story;
    }

    /** 스토리 목록 (keywords 포함) */
    @Transactional(readOnly = true)
    public List<StoryListResponse> getMyStories() {
        User user = getLoginUser();
        List<WrongAnswerStory> stories = wrongAnswerStoryRepository.findByUser(user);
        
        return stories.stream()
                .map(story -> {
                    List<String> keywords = storyWordListRepository.findByStoryIdWithWord(story.getStoryId())
                            .stream()
                            .map(swl -> swl.getWord().getWord())
                            .filter(word -> word != null && !word.trim().isEmpty())
                            .collect(Collectors.toList());
                    
                    return StoryListResponse.builder()
                            .storyId(story.getStoryId())
                            .title(story.getTitle())
                            .titleKo(story.getTitleKo())
                            .storyEn(story.getStoryEn())
                            .storyKo(story.getStoryKo())
                            .createdAt(story.getCreatedAt())
                            .keywords(keywords)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /** 스토리 상세 조회 */
    public WrongAnswerStory getStoryDetail(Long storyId) {
        return wrongAnswerStoryRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("스토리를 찾을 수 없습니다."));
    }

    /** 스토리에 사용된 오답 목록 조회 (Word 엔티티 함께 로딩) */
    @Transactional(readOnly = true)
    public List<StoryWordList> getWrongWordsInStory(Long storyId) {
        // FETCH JOIN을 사용하여 Word 엔티티를 함께 로딩 (LAZY 로딩 문제 해결)
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
