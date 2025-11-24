package com.wordweb.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key;

    // Access Token: 1시간
    private final long ACCESS_TOKEN_VALID_TIME = 1000L * 60 * 60;

    // Refresh Token: 2주
    private final long REFRESH_TOKEN_VALID_TIME = 1000L * 60 * 60 * 24 * 14;

    public JwtTokenProvider() {
        // HS256용 SecretKey (프로젝트 시작 시 1회 생성)
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }


    /* ===========================================
     *  1. Request Header에서 JWT 추출 (resolveToken)
     * =========================================== */
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);  // Bearer 제거 후 토큰 반환
        }
        return null;
    }


    /* ===========================================
     *  2. Access Token 발급
     * =========================================== */
    public String generateAccessToken(String email) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALID_TIME))
                .signWith(key)
                .compact();
    }


    /* ===========================================
     *  3. Refresh Token 발급
     * =========================================== */
    public String generateRefreshToken(String email) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_VALID_TIME))
                .signWith(key)
                .compact();
    }


    /* ===========================================
     *  4. JWT에서 이메일(Subject) 추출
     * =========================================== */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }


    /* ===========================================
     *  5. JWT 유효성 검사
     * =========================================== */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT 만료됨");
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("JWT 유효하지 않음");
        }

        return false;
    }
}
