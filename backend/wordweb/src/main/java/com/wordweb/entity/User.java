package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USERS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "USER_PW", nullable = false)
    private String password;

    @Column(name = "NICKNAME", nullable = false, unique = true)
    private String nickname;

    @Column(name = "USER_NAME", nullable = false)
    private String userName;

    @Column(name = "USER_BIRTH", nullable = false)
    private String userBirth;

    @Column(name = "PREFERENCE")
    private String preference;

    @Column(name = "GOAL")
    private String goal;

    @Column(name = "DAILY_WORD_GOAL")
    private Integer dailyWordGoal;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfile(String preference, String goal, Integer dailyWordGoal) {
        if (preference != null) this.preference = preference;
        if (goal != null) this.goal = goal;
        if (dailyWordGoal != null) this.dailyWordGoal = dailyWordGoal;
    }

    public void changeBirth(String userBirth) {
        this.userBirth = userBirth;
    }
}
