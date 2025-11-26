package com.wordweb.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtil {

    // 현재 로그인 유저 Email 가져오기
    public static String getLoginUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("현재 로그인한 사용자가 없습니다.");
        }

        Object principal = authentication.getPrincipal();

        // 1) principal이 UserDetails인 경우
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        // 2) principal이 String(email)인 경우 → JWT 필터에서 이렇게 넣고 있음
        if (principal instanceof String email) {
            return email;
        }

        throw new RuntimeException("인증 정보를 가져올 수 없습니다.");
    }

    // Dashboard 등에서 사용하는 Wrapper 메소드
    public static String getCurrentUserEmail() {
        return getLoginUserEmail();
    }
}
