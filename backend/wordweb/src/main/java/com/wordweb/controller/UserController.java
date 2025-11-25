package com.wordweb.controller;

import com.wordweb.dto.user.PasswordChangeRequest;
import com.wordweb.dto.user.UserResponse;
import com.wordweb.dto.user.UserUpdateRequest;
import com.wordweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    /** 내 정보 조회 */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo() {
        return ResponseEntity.ok(userService.getMyInfo());
    }

    /** 정보 수정 */
    @PatchMapping
    public ResponseEntity<UserResponse> updateUser(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    /** 비밀번호 변경 */
    @PatchMapping("/password")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }
}
