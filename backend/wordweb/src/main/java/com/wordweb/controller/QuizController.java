package com.wordweb.controller;

import com.wordweb.dto.learn.QuizQuestionResponse;
import com.wordweb.dto.learn.QuizResultRequest;
import com.wordweb.dto.learn.QuizResultResponse;
import com.wordweb.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * 퀴즈 생성 (normal | wrong)
     * @param mode normal(일반) | wrong(오답복습)
     * @param count 문항 수 (기본 10)
     * @param level 난이도 (1-6 또는 beginner/intermediate/advanced)
     * @param category 카테고리 (예: Daily Life, Technology 등)
     * @param wordIds 특정 단어 ID 목록 (콤마로 구분, 예: 1,2,3)
     */
    @GetMapping
    public ResponseEntity<List<QuizQuestionResponse>> getQuiz(
            @RequestParam(defaultValue = "normal") String mode,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<Long> wordIds
    ) {
        return ResponseEntity.ok(quizService.getQuiz(mode, count, level, category, wordIds));
    }

    /** 퀴즈 결과 저장 */
    @PostMapping("/result")
    public ResponseEntity<QuizResultResponse> saveResult(@RequestBody QuizResultRequest request) {
        // 요청 데이터 검증
        if (request == null) {
            throw new IllegalArgumentException("요청 데이터가 null입니다.");
        }
        
        if (request.getAnswers() == null) {
            throw new IllegalArgumentException("답안 목록이 null입니다.");
        }
        
        if (request.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("답안 목록이 비어있습니다.");
        }
        
        List<Long> wrongWordIds = quizService.saveResult(request);
        
        QuizResultResponse response = new QuizResultResponse();
        response.setSuccess(true);
        response.setMessage("퀴즈 결과 저장 완료");
        response.setWrongWordIds(wrongWordIds);
        
        return ResponseEntity.ok(response);
    }
}
