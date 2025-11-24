package com.wordweb.service;

import com.wordweb.config.jwt.JwtTokenProvider;
import com.wordweb.config.jwt.RefreshToken;
import com.wordweb.config.jwt.RefreshTokenRepository;
import com.wordweb.dto.auth.LoginRequest;
import com.wordweb.dto.auth.SignupRequest;
import com.wordweb.dto.auth.TokenResponse;
import com.wordweb.entity.User;
import com.wordweb.repository.UserRepository;

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

    /** 회원가입 */
    public void signup(SignupRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .userId(null)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .userName(request.getUserName())
                .userBirth(request.getUserBirth())
                .createdAt(Timestamp.from(Instant.now()))
                .updatedAt(Timestamp.from(Instant.now()))
                .build();

        userRepository.save(user);
    }

    /** 로그인 */
    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 1) Access Token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());

        // 2) Refresh Token
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // 3) Refresh Token DB에 저장(기존 값 덮어쓰기)
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .email(user.getEmail())
                        .refreshToken(refreshToken)
                        .build()
        );

        return new TokenResponse(accessToken, refreshToken);
    }


    /** Access Token 재발급 */
    public TokenResponse reissue(String refreshToken) {

        // 1) Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh Token이 만료되었습니다.");
        }

        // 2) Email 추출
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        // 3) 저장된 Refresh Token 조회
        RefreshToken saved = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("Refresh Token DB 없음. 다시 로그인 필요"));

        if (!saved.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("Refresh Token 일치하지 않음. 다시 로그인하세요.");
        }

        // 4) 새 Access Token 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(email);

        // 5) Refresh Token도 새로 발급 (보안 강화)
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);
        saved.setRefreshToken(newRefreshToken);
        refreshTokenRepository.save(saved);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
