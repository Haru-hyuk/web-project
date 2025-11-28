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
    private final BCryptPasswordEncoder passwordEncoder;

    /** 현재 로그인 유저 조회 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
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

        // 닉네임 변경
        if (request.getNickname() != null) {
            user.changeNickname(request.getNickname());
        }

        // 프로필 변경 (preference, goal, dailyWordGoal)
        if (request.getPreference() != null ||
            request.getGoal() != null ||
            request.getDailyWordGoal() != null) {

            user.updateProfile(
                    request.getPreference(),
                    request.getGoal(),
                    request.getDailyWordGoal()
            );
        }

        // 생년월일 변경 (엔티티에 changeBirth 추가 필요)
        if (request.getUserBirth() != null) {
            user.changeBirth(request.getUserBirth());
        }

        // save() 필요 없음 (dirty checking)
        return UserResponse.from(user);
    }

    /** 비밀번호 변경 */
    @Transactional
    public void changePassword(PasswordChangeRequest request) {

        User user = getLoginUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다.");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("새 비밀번호가 서로 일치하지 않습니다.");
        }

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }
}
