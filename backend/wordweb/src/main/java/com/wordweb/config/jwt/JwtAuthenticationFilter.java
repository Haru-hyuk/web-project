package com.wordweb.config.jwt;

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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1) 헤더에서 토큰 추출
            String token = jwtTokenProvider.resolveToken(request);

            // 2) 유효한 토큰이면 인증 세팅
            if (token != null && jwtTokenProvider.validateToken(token)) {

                String email = jwtTokenProvider.getEmailFromToken(token);

                // 권한이 없는 사용자 → 빈 리스트
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("USER"))
                        );

                // SecurityContext 에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            // JWT 오류 발생 → 스프링 Security 예외 핸들러로 이동
            request.setAttribute("jwtException", e);
        }

        filterChain.doFilter(request, response);
    }
}
