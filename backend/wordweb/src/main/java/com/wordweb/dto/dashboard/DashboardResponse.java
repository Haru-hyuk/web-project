package com.wordweb.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponse {

    private String nickname;
    private int dailyGoal;

    private int todayProgress;
    private int percentage;
    private int streak;

}
