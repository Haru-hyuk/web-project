package com.wordweb.service;

import com.wordweb.dto.auth.*;
import com.wordweb.entity.RefreshToken;
import com.wordweb.entity.User;
import com.wordweb.repository.RefreshTokenRepository;
import com.wordweb.repository.UserRepository;
import com.wordweb.config.jwt.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MailService mailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    /* =============================================
         회원가입
       ============================================= */
    @Transactional
    public void signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .userName(request.getUserName())
                .userBirth(request.getUserBirth())
                .preference(request.getPreference())  // 관심분야
                .goal(request.getGoal())              // 학습목표
                .dailyWordGoal(request.getDailyWordGoal() != null ? request.getDailyWordGoal() : 20)  // 하루 목표 단어 수
                .build();

        userRepository.save(user);
    }


    /* =============================================
         로그인 + JWT 발급
       ============================================= */
    @Transactional
    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String access = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refresh = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // 기존 refresh token 존재 여부 확인
        RefreshToken tokenEntity = refreshTokenRepository.findById(user.getEmail())
                .orElse(RefreshToken.builder()
                        .userEmail(user.getEmail())
                        .refreshToken(refresh)
                        .build());

        // 항상 새 refresh token으로 업데이트
        tokenEntity.setRefreshToken(refresh);
        refreshTokenRepository.save(tokenEntity);

        return new TokenResponse(access, refresh);
    }
    
    /** =============================
     * Refresh 토큰 재발급
     * ============================= */
    @Transactional
    public TokenResponse refresh(String refreshToken) {

        RefreshToken saved = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("리프레시 토큰이 유효하지 않습니다."));

        String email = saved.getUserEmail();

        String newAccess = jwtTokenProvider.generateAccessToken(email);
        String newRefresh = jwtTokenProvider.generateRefreshToken(email);

        saved.setRefreshToken(newRefresh);
        refreshTokenRepository.save(saved);

        return new TokenResponse(newAccess, newRefresh);
    }



    /* =============================================
         로그아웃
       ============================================= */
    @Transactional
    public void logout(String email) {
        refreshTokenRepository.deleteById(email);
    }


    /* =============================================
         이메일 찾기
       ============================================= */
    public FindEmailResponse findEmail(FindEmailRequest req) {

        User user = userRepository
                .findByUserNameAndUserBirth(req.getUserName(), req.getUserBirth())
                .orElseThrow(() -> new RuntimeException("일치하는 계정을 찾을 수 없습니다."));

        return new FindEmailResponse(user.getEmail());
    }


    /* =============================================
         비밀번호 재설정
       ============================================= */
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {

        User user = userRepository
                .findByUserNameAndEmail(req.getUserName(), req.getEmail())
                .orElseThrow(() -> new RuntimeException("일치하는 계정을 찾을 수 없습니다."));

        // 임시 비밀번호 메일 전송
        String tempPassword = mailService.sendTempPassword(req.getEmail());

        // 엔티티 메서드 사용 → setter 금지
        user.changePassword(passwordEncoder.encode(tempPassword));

        // updatedAt 자동 반영됨 (@PreUpdate)
        // userRepository.save(user); // 필요 없음 (dirty checking)
    }
}
