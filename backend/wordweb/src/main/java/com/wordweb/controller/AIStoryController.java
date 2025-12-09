package com.wordweb.controller;

import com.wordweb.dto.ai.AIStoryRequest;
import com.wordweb.dto.ai.AIStoryResponse;
import com.wordweb.service.AIStoryService;
import com.wordweb.service.AIStoryService.StoryResult;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

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

        // 🔥 Null-safe: wrongAnswerLogIds가 null이어도 빈 리스트로 처리
        List<Long> wrongIds = request.getWrongAnswerLogIds() != null
                ? Arrays.asList(request.getWrongAnswerLogIds())
                : List.of();

        // AI + 저장
        StoryResult result = aiStoryService.generateAndSaveStory(
                wrongIds,
                request.getDifficulty(),
                request.getStyle()
        );

        if (!result.isSuccess()) {
            return ResponseEntity.status(500).body(
                    new AIStoryResponse(
                            false,
                            "AI 스토리 생성 실패",
                            "AI 스토리 생성 실패",
                            "",
                            "",
                            null,
                            null
                    )
            );
        }

        return ResponseEntity.ok(
                AIStoryResponse.builder()
                        .success(true)
                        .message("스토리 생성 성공")
                        .title(result.getTitle())
                        .storyEn(result.getStoryEn())
                        .storyKo(result.getStoryKo())
                        .usedWords(result.getUsedWords())
                        .storyId(result.getStoryId())
                        .build()
        );
    }
}
