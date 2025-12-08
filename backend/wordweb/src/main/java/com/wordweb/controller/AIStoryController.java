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
    	        Arrays.asList(request.getWrongAnswerLogIds()),  // WRONG_ANSWER_LOG의 PK 목록
    	        request.getDifficulty(),
    	        request.getStyle()
    	);


        if (!result.isSuccess()) {
            return ResponseEntity.status(500).body(
                    new AIStoryResponse(
                            false,
                            "AI 스토리 생성 실패",
                            "AI 스토리 생성 실패",  // title
                            "",
                            "",
                            null,
                            null  // storyId는 실패 시 null
                    )
            );
        }

        return ResponseEntity.ok(
                AIStoryResponse.builder()
                        .success(true)
                        .message("스토리 생성 성공")
                        .title(result.getTitle())  // AI 생성 제목 포함
                        .storyEn(result.getStoryEn())
                        .storyKo(result.getStoryKo())
                        .usedWords(result.getUsedWords())
                        .storyId(result.getStoryId())
                        .build()
        );
    }
}
