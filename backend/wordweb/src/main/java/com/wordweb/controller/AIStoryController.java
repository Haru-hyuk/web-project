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
     * 
     * Request Body:
     * {
     *   "wrongAnswerLogIds": [1, 2, 3, ...]  // 최소 1개, 최대 20개
     * }
     */
    @PostMapping("/story")
    public ResponseEntity<AIStoryResponse> generateStory(@RequestBody AIStoryRequest request) {

        // wrongAnswerLogIds 검증
        if (request.getWrongAnswerLogIds() == null || request.getWrongAnswerLogIds().length == 0) {
            return ResponseEntity.badRequest().body(
                    new AIStoryResponse(
                            false,
                            "단어를 최소 1개 이상 선택해주세요.",
                            "잘못된 요청",
                            "잘못된 요청",
                            "",
                            "",
                            null,
                            null
                    )
            );
        }

        // 최대 20개 제한 검증
        if (request.getWrongAnswerLogIds().length > 20) {
            return ResponseEntity.badRequest().body(
                    new AIStoryResponse(
                            false,
                            "스토리 생성에는 최대 20개의 단어만 사용할 수 있습니다.",
                            "단어 개수 초과",
                            "단어 개수 초과",
                            "",
                            "",
                            null,
                            null
                    )
            );
        }

        // wrongWordIds 기반으로 스토리 생성 + 저장 
    	StoryResult result = aiStoryService.generateAndSaveStory(
    	        Arrays.asList(request.getWrongAnswerLogIds())  // WRONG_ANSWER_LOG의 PK 목록
    	);


        if (!result.isSuccess()) {
            return ResponseEntity.status(500).body(
                    new AIStoryResponse(
                            false,
                            "AI 스토리 생성 실패",
                            "AI 스토리 생성 실패",  // title
                            "AI 스토리 생성 실패",  // titleKo
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
                        .title(result.getTitle())  // AI 생성 제목 (영어)
                        .titleKo(result.getTitleKo())  // AI 생성 제목 (한글)
                        .storyEn(result.getStoryEn())
                        .storyKo(result.getStoryKo())
                        .usedWords(result.getUsedWords())
                        .storyId(result.getStoryId())
                        .build()
        );
    }
}
