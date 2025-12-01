package com.wordweb.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String email;
    private String password;
    private String nickname;
    private String userName;
    private String userBirth;
    private String preference;      // 관심분야
    private String goal;             // 학습목표
    private Integer dailyWordGoal;   // 하루 목표 단어 수
}
