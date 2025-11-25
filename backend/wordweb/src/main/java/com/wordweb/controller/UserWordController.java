package com.wordweb.controller;

import com.wordweb.entity.UserWord;
import com.wordweb.service.UserWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-word")
@RequiredArgsConstructor
public class UserWordController {

    private final UserWordService userWordService;

    /** 학습 결과 기록 */
    @PostMapping("/{wordId}")
    public ResponseEntity<String> studyWord(
            @PathVariable Long wordId,
            @RequestParam("result") String result,
            Authentication auth
    ) {
        String email = auth.getPrincipal().toString();
        userWordService.studyWord(email, wordId, result);
        return ResponseEntity.ok("학습 기록이 저장되었습니다.");
    }

    /** 학습 기록 조회 */
    @GetMapping
    public ResponseEntity<List<UserWord>> getStudyList(Authentication auth) {
        String email = auth.getPrincipal().toString();
        return ResponseEntity.ok(userWordService.getMyStudyList(email));
    }
}
