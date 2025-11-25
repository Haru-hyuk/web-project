package com.wordweb.service;

import com.wordweb.dto.user.PasswordChangeRequest;
import com.wordweb.dto.user.UserResponse;
import com.wordweb.dto.user.UserUpdateRequest;
import com.wordweb.entity.User;
import com.wordweb.repository.UserRepository;
import com.wordweb.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 현재 로그인 유저 조회 */
    private User getLoginUser() {
        String email = SecurityUtil.getLoginUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    /** 내 정보 조회 */
    public UserResponse getMyInfo() {
        return UserResponse.from(getLoginUser());
    }

    /** 내 정보 수정 */
    @Transactional
    public UserResponse updateUser(UserUpdateRequest request) {
        User user = getLoginUser();

        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getPreference() != null) user.setPreference(request.getPreference());
        if (request.getGoal() != null) user.setGoal(request.getGoal());
        if (request.getDailyWordGoal() != null) user.setDailyWordGoal(request.getDailyWordGoal());
        if (request.getUserBirth() != null) user.setUserBirth(request.getUserBirth());

        return UserResponse.from(user);
    }

    /** 비밀번호 변경 */
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        User user = getLoginUser();

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 새로운 비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }
}
