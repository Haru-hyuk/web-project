package com.wordweb.service;

import com.wordweb.config.jwt.JwtTokenProvider;
import com.wordweb.dto.auth.LoginRequest;
import com.wordweb.dto.auth.SignupRequest;
import com.wordweb.dto.auth.TokenResponse;
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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    /** ===========================
     *        회원가입
     *  =========================== */
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
                .dailyWordGoal(20)
                .createdAt(Timestamp.from(Instant.now()))
                .updatedAt(Timestamp.from(Instant.now()))
                .build();

        userRepository.save(user);
    }


    /** ===========================
     *           로그인
     *  =========================== */
    @Transactional
    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }

        // 1) AccessToken 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());

        // 2) RefreshToken 발급
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // 3) 기존 RefreshToken 삭제 후 새로 저장
        refreshTokenRepository.deleteById(user.getEmail());

        RefreshToken tokenEntity = RefreshToken.builder()
                .userEmail(user.getEmail())
                .refreshToken(refreshToken)
                .build();

        refreshTokenRepository.save(tokenEntity);

        return new TokenResponse(accessToken, refreshToken);
    }


    /** ===========================
     *       Refresh Token 재발급
     *  =========================== */
    @Transactional
    public TokenResponse refresh(String refreshToken) {

        // 1) 토큰 자체 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        // 2) email 추출
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        // 3) DB에 저장된 Refresh Token 조회
        RefreshToken savedToken = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("저장된 Refresh Token이 없습니다."));

        // 4) 토큰 일치 여부 확인
        if (!savedToken.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("Refresh Token이 일치하지 않습니다.");
        }

        // 5) Access Token 재발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(email);

        // 6) (옵션) RefreshToken도 재발급
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);
        savedToken.setRefreshToken(newRefreshToken);
        refreshTokenRepository.save(savedToken);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }


    /** ===========================
     *            로그아웃
     *  =========================== */
    @Transactional
    public void logout(String email) {
        refreshTokenRepository.deleteById(email);
    }
}
