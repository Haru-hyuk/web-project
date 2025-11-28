package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "REFRESH_TOKEN")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @Column(name = "USER_EMAIL", nullable = false)
    private String userEmail;

    @Column(name = "REFRESH_TOKEN", nullable = false)
    private String refreshToken;

    /** refresh token 갱신 메서드 */
    public void updateToken(String newToken) {
        this.refreshToken = newToken;
    }
}
