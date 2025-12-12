package com.wordweb.controller;

import com.wordweb.dto.ai.AIStoryRequest;
import com.wordweb.dto.ai.AIStoryResponse;
import com.wordweb.dto.ai.AIStoryResult;
import com.wordweb.service.AIStoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

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
    public ResponseEntity<AIStoryResponse> generateStory(
            @RequestBody AIStoryRequest request
    ) {

        // WRONG_ANSWER_LOG의 PK 목록 기반으로 스토리 생성 + 저장
        AIStoryResult result = aiStoryService.generateAndSaveStory(
                Arrays.asList(request.getWrongAnswerLogIds())
        );

        // ❌ 실패 케이스
        if (!result.isSuccess()) {
            return ResponseEntity.status(500).body(
                    AIStoryResponse.builder()
                            .success(false)
                            .message("AI 스토리 생성 실패")
                            .title(null)
                            .titleKo(null)
                            .storyEn("")
                            .storyKo("")
                            .usedWords(null)
                            .storyId(null)
                            .build()
            );
        }

        // ✅ 성공 케이스
        return ResponseEntity.ok(
                AIStoryResponse.builder()
                        .success(true)
                        .message("스토리 생성 성공")

                        // ✅ 제목 분리
                        .title(result.getTitle())
                        .titleKo(result.getTitleKo())

                        .storyEn(result.getStoryEn())
                        .storyKo(result.getStoryKo())
                        .usedWords(result.getUsedWords())
                        .storyId(result.getStoryId())
                        .build()
        );
    }
}
