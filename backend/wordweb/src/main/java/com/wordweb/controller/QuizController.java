package com.wordweb.controller;

import com.wordweb.dto.learn.QuizQuestionResponse;
import com.wordweb.dto.learn.QuizResultRequest;
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

    /** 퀴즈 생성 (normal | wrong) */
    @GetMapping
    public ResponseEntity<List<QuizQuestionResponse>> getQuiz(
            @RequestParam(defaultValue = "normal") String mode
    ) {
        return ResponseEntity.ok(quizService.getQuiz(mode));
    }

    /** 퀴즈 결과 저장 */
    @PostMapping("/result")
    public ResponseEntity<String> saveResult(@RequestBody QuizResultRequest request) {
        quizService.saveResult(request);
        return ResponseEntity.ok("퀴즈 결과 저장 완료");
    }
}
