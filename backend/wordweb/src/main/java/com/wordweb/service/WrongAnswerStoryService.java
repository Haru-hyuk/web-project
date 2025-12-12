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

    /** ✅ AI 스토리 생성 후 저장 (영문 + 한글 제목 모두 저장) */
    @Transactional
    public WrongAnswerStory createStory(
            String title,
            String titleKo,
            String storyEn,
            String storyKo,
            List<Long> wrongLogIds
    ) {
        User user = getLoginUser();

        // 1) 스토리 저장
        WrongAnswerStory story = WrongAnswerStory.create(
                user,
                title,
                titleKo,
                storyEn,
                storyKo
        );
        wrongAnswerStoryRepository.save(story);

        // 2) StoryWordList 저장
        for (Long wrongLogId : wrongLogIds) {
            wrongAnswerLogRepository.findById(wrongLogId)
                    .ifPresent(log -> {
                        StoryWordList relation = StoryWordList.create(
                                story.getStoryId(),
                                log.getWord().getWordId(),
                                wrongLogId
                        );
                        storyWordListRepository.save(relation);
                    });
        }

        // 3) 오답 로그 사용 처리
        wrongAnswerLogRepository.findAllById(wrongLogIds)
                .forEach(WrongAnswerLog::markUsedInStory);

        return story;
    }

    /** 스토리 목록 조회 */
    @Transactional(readOnly = true)
    public List<StoryListResponse> getMyStories() {
        User user = getLoginUser();
        List<WrongAnswerStory> stories = wrongAnswerStoryRepository.findByUser(user);

        return stories.stream()
                .map(story -> {
                    List<String> keywords = storyWordListRepository
                            .findByStoryIdWithWord(story.getStoryId())
                            .stream()
                            .map(swl -> swl.getWord().getWord())
                            .filter(word -> word != null && !word.isBlank())
                            .collect(Collectors.toList());

                    return StoryListResponse.builder()
                            .storyId(story.getStoryId())
                            .title(story.getTitle())       // 영어 제목
                            .titleKo(story.getTitleKo())   // 한글 제목
                            .storyEn(story.getStoryEn())
                            .storyKo(story.getStoryKo())
                            .createdAt(story.getCreatedAt())
                            .keywords(keywords)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /** 스토리 상세 조회 */
    @Transactional(readOnly = true)
    public StoryListResponse getStoryDetail(Long storyId) {

        WrongAnswerStory story = wrongAnswerStoryRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("스토리를 찾을 수 없습니다."));

        List<String> keywords = storyWordListRepository
                .findByStoryIdWithWord(storyId)
                .stream()
                .map(swl -> swl.getWord().getWord())
                .filter(word -> word != null && !word.isBlank())
                .collect(Collectors.toList());

        return StoryListResponse.builder()
                .storyId(story.getStoryId())
                .title(story.getTitle())       // 영어 제목
                .titleKo(story.getTitleKo())   // 한글 제목
                .storyEn(story.getStoryEn())
                .storyKo(story.getStoryKo())
                .createdAt(story.getCreatedAt())
                .keywords(keywords)
                .build();
    }

    /** 오답 단어 조회 */
    @Transactional(readOnly = true)
    public List<StoryWordList> getWrongWordsInStory(Long storyId) {
        return storyWordListRepository.findByStoryIdWithWord(storyId);
    }

    /** 스토리 삭제 */
    @Transactional
    public void deleteStory(Long storyId) {
        User user = getLoginUser();

        WrongAnswerStory story = wrongAnswerStoryRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("스토리를 찾을 수 없습니다."));

        if (!story.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("본인 스토리만 삭제할 수 있습니다.");
        }

        storyWordListRepository.deleteByStoryId(storyId);
        wrongAnswerStoryRepository.delete(story);
    }
}
