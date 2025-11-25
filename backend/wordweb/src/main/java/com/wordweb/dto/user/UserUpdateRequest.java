package com.wordweb.dto.user;

import lombok.Getter;

@Getter
public class UserUpdateRequest {
    private String nickname;
    private String preference;
    private String goal;
    private Integer dailyWordGoal;
    private String userBirth;
}
