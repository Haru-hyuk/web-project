package com.wordweb.controller;

import com.wordweb.dto.auth.LoginRequest;
import com.wordweb.dto.auth.SignupRequest;
import com.wordweb.dto.auth.TokenResponse;
import com.wordweb.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /** 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    /** 로그인 → Access + Refresh 발급 & DB 저장 */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /** Refresh Token으로 Access Token 재발급 */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody Map<String, String> req) {
        String refreshToken = req.get("refreshToken");
        return ResponseEntity.ok(authService.reissue(refreshToken));
    }
}

