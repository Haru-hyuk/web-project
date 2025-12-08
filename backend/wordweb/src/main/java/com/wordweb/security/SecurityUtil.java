package com.wordweb.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtil {

    // 현재 로그인 유저 Email 가져오기
    public static String getLoginUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            System.err.println("[SecurityUtil] Authentication이 null입니다.");
            throw new RuntimeException("현재 로그인한 사용자가 없습니다. (Authentication is null)");
        }

        if (!authentication.isAuthenticated()) {
            System.err.println("[SecurityUtil] Authentication이 인증되지 않았습니다. Principal: " + authentication.getPrincipal());
            throw new RuntimeException("현재 로그인한 사용자가 없습니다. (Not authenticated)");
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

        System.err.println("[SecurityUtil] 알 수 없는 Principal 타입: " + (principal != null ? principal.getClass().getName() : "null"));
        throw new RuntimeException("인증 정보를 가져올 수 없습니다. Principal type: " + (principal != null ? principal.getClass().getName() : "null"));
    }

    // Dashboard 등에서 사용하는 Wrapper 메소드
    public static String getCurrentUserEmail() {
        return getLoginUserEmail();
    }
}
