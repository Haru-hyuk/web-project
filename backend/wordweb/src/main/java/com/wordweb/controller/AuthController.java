package com.wordweb.controller;

import com.wordweb.dto.auth.*;
import com.wordweb.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest req) {
        authService.signup(req);
        return ResponseEntity.ok("회원가입 완료");
    }

    /** 로그인 */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    /** Refresh 토큰 재발급 */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req.getRefreshToken()));
    }

    /** 로그아웃 */
    @PostMapping("/logout/{email}")
    public ResponseEntity<String> logout(@PathVariable String email) {
        authService.logout(email);
        return ResponseEntity.ok("로그아웃 완료");
    }

    /** 이메일 찾기 */
    @PostMapping("/find-email")
    public ResponseEntity<?> findEmail(@RequestBody FindEmailRequest req) {
        return ResponseEntity.ok(authService.findEmail(req));
    }

    /** 비밀번호 재설정 */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(Map.of("message", "임시 비밀번호가 이메일로 발송되었습니다."));
    }
}