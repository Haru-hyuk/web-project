package com.wordweb.controller;

import com.wordweb.entity.WrongWord;
import com.wordweb.service.WrongWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wrong")
@RequiredArgsConstructor
public class WrongWordController {

    private final WrongWordService wrongWordService;

    /** 오답 추가 */
    @PostMapping("/{wordId}")
    public ResponseEntity<String> addWrongWord(
            @PathVariable Long wordId,
            @RequestParam(required = false, defaultValue = "") String tag,
            Authentication auth
    ) {
        String email = auth.getPrincipal().toString();
        wrongWordService.addWrongWord(email, wordId, tag);
        return ResponseEntity.ok("오답 단어로 등록되었습니다.");
    }

    /** 오답 삭제 */
    @DeleteMapping("/{wordId}")
    public ResponseEntity<String> removeWrongWord(
            @PathVariable Long wordId,
            Authentication auth
    ) {
        String email = auth.getPrincipal().toString();
        wrongWordService.removeWrongWord(email, wordId);
        return ResponseEntity.ok("오답 단어에서 제거되었습니다.");
    }

    /** 오답 목록 조회 */
    @GetMapping
    public ResponseEntity<List<WrongWord>> getWrongWordList(Authentication auth) {
        String email = auth.getPrincipal().toString();
        return ResponseEntity.ok(wrongWordService.getWrongWordList(email));
    }
}
