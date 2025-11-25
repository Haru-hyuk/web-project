package com.wordweb.service;

import com.wordweb.entity.Story;
import com.wordweb.entity.User;
import com.wordweb.repository.StoryRepository;
import com.wordweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final AIStoryService aiStoryService;

    /** AI 자동 생성 스토리 */
    public Story createAIStory(String email, String[] wordList) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        AIStoryService.StoryResult result =
                aiStoryService.generateStory(wordList, "intermediate", "narrative");

        Story story = Story.builder()
                .user(user)
                .title("AI Generated Story")
                .storyEn(result.getStoryEn())
                .storyKo(result.getStoryKo())
                .targetWordIds(String.join(",", wordList))
                .createdAt(Timestamp.from(Instant.now()))
                .build();

        return storyRepository.save(story);
    }

    /** 내 스토리 목록 조회 */
    public List<Story> getMyStories(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return storyRepository.findAllByUser(user);
    }

    /** 스토리 상세 조회 */
    public Story getStory(Long id) {
        return storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("스토리를 찾을 수 없습니다."));
    }
}
