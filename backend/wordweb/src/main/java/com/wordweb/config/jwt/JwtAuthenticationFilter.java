package com.wordweb.config.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // 인증 불필요한 API
        return uri.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtTokenProvider.resolveToken(request);

        if (token != null) {
            try {
                jwtTokenProvider.validateTokenOrThrow(token);

                String email = jwtTokenProvider.getEmailFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("USER"))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ExpiredJwtException e) {
                // 토큰 만료 시 로그만 남기고 계속 진행 (401은 SecurityConfig에서 처리)
                System.err.println("JWT 토큰 만료: " + e.getMessage());
                request.setAttribute("jwtException", e);
            } catch (JwtException | IllegalArgumentException e) {
                // 유효하지 않은 토큰
                System.err.println("JWT 토큰 검증 실패: " + e.getMessage());
                request.setAttribute("jwtException", e);
            } catch (Exception e) {
                // 기타 예외
                System.err.println("JWT 필터 예외: " + e.getMessage());
                e.printStackTrace();
                request.setAttribute("jwtException", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
