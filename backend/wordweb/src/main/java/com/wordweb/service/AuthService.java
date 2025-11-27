package com.wordweb.service;

import com.wordweb.config.jwt.JwtTokenProvider;
import com.wordweb.dto.auth.*;
import com.wordweb.entity.RefreshToken;
import com.wordweb.entity.User;
import com.wordweb.repository.RefreshTokenRepository;
import com.wordweb.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MailService mailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 회원가입 */
    @Transactional
    public void signup(SignupRequest request) {


        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
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
                .createdAt(Timestamp.from(Instant.now()))
                .updatedAt(Timestamp.from(Instant.now()))
                .build();

        userRepository.save(user);
    }

    /** 로그인 */
    @Transactional
    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // 기존 Refresh Token 제거 후 새로 저장
        refreshTokenRepository.deleteById(user.getEmail());

        RefreshToken tokenEntity = RefreshToken.builder()
                .userEmail(user.getEmail())
                .refreshToken(refreshToken)
                .build();

        refreshTokenRepository.save(tokenEntity);

        return new TokenResponse(accessToken, refreshToken);
    }

    /** Refresh Token 재발급 */
    @Transactional
    public TokenResponse refresh(String refreshToken) {

        try {
            jwtTokenProvider.validateTokenOrThrow(refreshToken);
        } catch (Exception e) {
            throw new RuntimeException("Refresh Token이 만료되었습니다.");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        RefreshToken saved = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("저장된 Refresh Token이 없습니다."));

        if (!saved.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("Refresh Token이 일치하지 않습니다.");
        }

        String newAccess = jwtTokenProvider.generateAccessToken(email);
        String newRefresh = jwtTokenProvider.generateRefreshToken(email);

        saved.setRefreshToken(newRefresh);
        refreshTokenRepository.save(saved);

        return new TokenResponse(newAccess, newRefresh);
    }

    /** 로그아웃 */
    @Transactional
    public void logout(String email) {
        refreshTokenRepository.deleteById(email);
    }

    /** 이메일 찾기 */
    public FindEmailResponse findEmail(FindEmailRequest req) {

        User user = userRepository
                .findByUserNameAndUserBirth(req.getUserName(), req.getUserBirth())
                .orElseThrow(() -> new RuntimeException("등록된 계정을 찾을 수 없습니다."));

        return new FindEmailResponse(user.getEmail());
    }

    /** 비밀번호 재설정 */
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {

        User user = userRepository.findByUserNameAndEmail(req.getUserName(), req.getEmail())
                .orElseThrow(() -> new RuntimeException("일치하는 계정이 없습니다."));

        // 임시 비밀번호 생성 + 이메일 발송
        String tempPassword = mailService.sendTempPassword(req.getEmail());

        // 암호화 후 저장
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);
    }
}
