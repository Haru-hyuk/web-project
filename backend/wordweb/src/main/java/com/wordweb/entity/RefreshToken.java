package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "REFRESH_TOKEN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @Column(name = "USER_EMAIL")
    private String userEmail;

    @Column(name = "REFRESH_TOKEN")
    private String refreshToken;
}
