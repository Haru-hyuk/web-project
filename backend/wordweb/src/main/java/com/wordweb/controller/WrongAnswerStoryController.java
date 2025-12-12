package com.wordweb.controller;

import com.wordweb.dto.story.StoryListResponse;
import com.wordweb.entity.StoryWordList;
import com.wordweb.entity.WrongAnswerStory;
import com.wordweb.service.WrongAnswerStoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/story")
public class WrongAnswerStoryController {

    private final WrongAnswerStoryService wrongAnswerStoryService;

    /**
     * AI 스토리 저장
     * POST /api/story
     * body:
     * {
     *   "title": "The Lord's Unspoken Shelter",
     *   "titleKo": "주님의 말없는 피난처",
     *   "storyEn": "...",
     *   "storyKo": "...",
     *   "wrongLogIds": [1,2,3]
     * }
     */
    @PostMapping
    public ResponseEntity<WrongAnswerStory> createStory(
            @RequestBody CreateStoryRequest request
    ) {
        WrongAnswerStory saved = wrongAnswerStoryService.createStory(
                request.getTitle(),     // ✅ 영어 제목
                request.getTitleKo(),   // ✅ 한글 제목
                request.getStoryEn(),
                request.getStoryKo(),
                request.getWrongLogIds()
        );

        return ResponseEntity.ok(saved);
    }

    /**
     * 스토리 생성 요청 DTO
     */
    public static class CreateStoryRequest {

        private String title;      // 영어 제목
        private String titleKo;    // 한글 제목
        private String storyEn;
        private String storyKo;
        private List<Long> wrongLogIds;

        public String getTitle() {
            return title;
        }

        public String getTitleKo() {
            return titleKo;
        }

        public String getStoryEn() {
            return storyEn;
        }

        public String getStoryKo() {
            return storyKo;
        }

        public List<Long> getWrongLogIds() {
            return wrongLogIds;
        }
    }

    /**
     * 내 스토리 목록 조회
     * GET /api/story
     */
    @GetMapping
    public ResponseEntity<List<StoryListResponse>> getMyStories() {
        return ResponseEntity.ok(
                wrongAnswerStoryService.getMyStories()
        );
    }

    /**
     * 스토리 상세 조회
     * GET /api/story/{storyId}
     */
    @GetMapping("/{storyId}")
    public ResponseEntity<StoryListResponse> getStoryDetail(
            @PathVariable Long storyId
    ) {
        return ResponseEntity.ok(
                wrongAnswerStoryService.getStoryDetail(storyId)
        );
    }

    /**
     * 스토리에 사용된 오답 단어 목록 조회
     * GET /api/story/{storyId}/words
     */
    @GetMapping("/{storyId}/words")
    public ResponseEntity<List<StoryWordList>> getWrongWordsInStory(
            @PathVariable Long storyId
    ) {
        return ResponseEntity.ok(
                wrongAnswerStoryService.getWrongWordsInStory(storyId)
        );
    }

    /**
     * 스토리 삭제
     * DELETE /api/story/{storyId}
     */
    @DeleteMapping("/{storyId}")
    public ResponseEntity<Void> deleteStory(
            @PathVariable Long storyId
    ) {
        wrongAnswerStoryService.deleteStory(storyId);
        return ResponseEntity.noContent().build();
    }
}
