package com.wordweb.controller;

import com.wordweb.entity.Story;
import com.wordweb.service.StoryService;
import com.wordweb.service.AIStoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/story")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;
    private final AIStoryService aiStoryService;

    /** ============================================
     *  1) AI 스토리 생성 결과만 반환 (DB 저장 X)
     * ============================================ */
    @PostMapping("/ai")
    public ResponseEntity<AIStoryService.StoryResult> generateAIStory(
            @RequestBody Map<String, Object> request,
            Authentication auth
    ) {
        String email = auth.getPrincipal().toString();

        List<String> wordList = (List<String>) request.get("words");
        String difficulty = (String) request.getOrDefault("difficulty", "intermediate");
        String style = (String) request.getOrDefault("style", "narrative");

        String[] words = wordList.toArray(new String[0]);

        AIStoryService.StoryResult result =
                aiStoryService.generateStory(words, difficulty, style);

        return ResponseEntity.ok(result);
    }


    /** ============================================
     *  2) AI 스토리 생성 + DB 저장
     * ============================================ */
    @PostMapping("/ai/save")
    public ResponseEntity<Story> createAIStoryAndSave(
            @RequestBody Map<String, Object> request,
            Authentication auth
    ) {
        String email = auth.getPrincipal().toString();

        List<String> wordList = (List<String>) request.get("words");
        String[] words = wordList.toArray(new String[0]);

        Story created = storyService.createAIStory(email, words);
        return ResponseEntity.ok(created);
    }


    /** ============================================
     *  내 스토리 목록 조회
     * ============================================ */
    @GetMapping
    public ResponseEntity<List<Story>> myStories(Authentication auth) {
        String email = auth.getPrincipal().toString();
        return ResponseEntity.ok(storyService.getMyStories(email));
    }

    /** ============================================
     *  스토리 상세 조회
     * ============================================ */
    @GetMapping("/{storyId}")
    public ResponseEntity<Story> storyDetail(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.getStory(storyId));
    }
}
