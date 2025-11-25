package com.wordweb.dto.user;

import com.wordweb.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String userName;
    private String userBirth;
    private String preference;
    private String goal;
    private Integer dailyWordGoal;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .userName(user.getUserName())
                .userBirth(user.getUserBirth())
                .preference(user.getPreference())
                .goal(user.getGoal())
                .dailyWordGoal(user.getDailyWordGoal())
                .build();
    }
}
