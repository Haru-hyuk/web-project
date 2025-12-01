package com.wordweb.dto.ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIStoryRequest {

    // wrongAnswerLog의 ID 목록
    private Long[] wrongWordIds;

    private String difficulty;
    private String style;
}
