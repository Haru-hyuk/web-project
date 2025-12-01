package com.wordweb.controller;

import com.wordweb.dto.ai.AIStoryRequest;
import com.wordweb.dto.ai.AIStoryResponse;
import com.wordweb.service.AIStoryService;
import com.wordweb.service.AIStoryService.StoryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AIStoryController {

    private final AIStoryService aiStoryService;

    /**
     * AI 스토리 생성
     * POST /api/ai/story
     * body 예시:
     * {
     *   "words": ["apple", "tree"],
     *   "difficulty": "easy",
     *   "style": "funny"
     * }
     */
    @PostMapping("/story")
    public ResponseEntity<AIStoryResponse> generateStory(@RequestBody AIStoryRequest request) {

        StoryResult result = aiStoryService.generateStory(
                request.getWords(),
                request.getDifficulty(),
                request.getStyle()
        );

        // 실패
        if (!result.isSuccess()) {
            return ResponseEntity.status(500).body(
                    new AIStoryResponse(
                            false,
                            "AI 스토리 생성 실패",
                            "",
                            "",
                            null
                    )
            );
        }

        // 성공
        return ResponseEntity.ok(
                new AIStoryResponse(
                        true,
                        "스토리 생성 성공",
                        result.getStoryEn(),
                        result.getStoryKo(),
                        result.getUsedWords()
                )
        );
    }
}
