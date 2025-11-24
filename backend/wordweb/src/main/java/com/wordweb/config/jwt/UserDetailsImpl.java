package com.wordweb.config.jwt;

import com.wordweb.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 사용 안함
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();    // 엔터티 password 그대로 매칭
    }

    @Override
    public String getUsername() {
        return user.getEmail();       // 로그인 식별자는 email 사용
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /** 추가적으로 User 엔티티 접근 필요하면 사용 */
    public Long getUserId() {
        return user.getUserId();
    }

    public User getUser() {
        return user;
    }
}

