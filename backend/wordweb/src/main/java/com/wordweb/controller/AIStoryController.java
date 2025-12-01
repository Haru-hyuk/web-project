package com.wordweb.controller;

import com.wordweb.dto.ai.AIStoryRequest;
import com.wordweb.dto.ai.AIStoryResponse;
import com.wordweb.service.AIStoryService;
import com.wordweb.service.AIStoryService.StoryResult;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AIStoryController {

    private final AIStoryService aiStoryService;

    /**
     * AI 스토리 생성 + DB 저장
     * POST /api/ai/story
     */
    @PostMapping("/story")
    public ResponseEntity<AIStoryResponse> generateStory(@RequestBody AIStoryRequest request) {

        // wrongWordIds 기반으로 스토리 생성 + 저장
    	StoryResult result = aiStoryService.generateAndSaveStory(
    	        Arrays.asList(request.getWrongWordIds()),  // ← 변환
    	        request.getDifficulty(),
    	        request.getStyle()
    	);


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
