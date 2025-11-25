package com.wordweb.controller;

import com.wordweb.dto.auth.LoginRequest;
import com.wordweb.dto.auth.SignupRequest;
import com.wordweb.dto.auth.TokenResponse;
import com.wordweb.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest req) {
        authService.signup(req);
        return ResponseEntity.ok("회원가입 완료");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody String refreshToken) {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout/{email}")
    public ResponseEntity<String> logout(@PathVariable String email) {
        authService.logout(email);
        return ResponseEntity.ok("로그아웃 완료");
    }
}

