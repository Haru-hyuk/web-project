package com.wordweb.service;

import com.wordweb.entity.User;
import com.wordweb.repository.UserRepository;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /** 현재 로그인한 유저 조회 */
    private User getCurrentUser() {
        String email = SecurityUtil.getLoginUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    /** 하루 목표 단어 수 조회 */
    public int getDailyWordGoal() {
        User user = getCurrentUser();
        return user.getDailyWordGoal();
    }

    /** 하루 목표 단어 수 업데이트 */
    public void updateDailyWordGoal(int goal) {

        if (goal < 1) {
            throw new RuntimeException("목표 단어 수는 최소 1 이상이어야 합니다.");
        }

        User user = getCurrentUser();
        user.setDailyWordGoal(goal);
        user.setUpdatedAt(Timestamp.from(Instant.now()));

        userRepository.save(user);
    }

    /** 프로필 조회 */
    public User getMyProfile() {
        return getCurrentUser();
    }
}
