package com.wordweb.controller;

import com.wordweb.dto.user.UserResponse;
import com.wordweb.entity.User;
import com.wordweb.service.UserService;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    /** 하루 목표 단어 수 조회 */
    @GetMapping("/daily-goal")
    public ResponseEntity<Integer> getDailyGoal() {
        int goal = userService.getDailyWordGoal();
        return ResponseEntity.ok(goal);
    }

    /** 하루 목표 단어 수 변경 */
    @PutMapping("/daily-goal")
    public ResponseEntity<String> updateDailyGoal(@RequestParam int goal) {
        userService.updateDailyWordGoal(goal);
        return ResponseEntity.ok("하루 목표 단어 수가 변경되었습니다.");
    }

    /** 프로필 조회 */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getMyProfile() {

        User user = userService.getMyProfile();

        return ResponseEntity.ok(UserResponse.from(user));
    }

}
