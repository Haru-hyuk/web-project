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
    /** 이메일 중복 확인 */
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        boolean exists = authService.checkEmailDuplicate(email);

        return ResponseEntity.ok(Map.of(
                "exists", exists,
                "message", exists ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다."
        ));
    }

    /** 닉네임 중복 확인 */
    @PostMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestBody Map<String, String> body) {
        String nickname = body.get("nickname");

        boolean exists = authService.checkNicknameDuplicate(nickname);

        return ResponseEntity.ok(Map.of(
                "exists", exists,
                "message", exists ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다."
        ));
    }

}