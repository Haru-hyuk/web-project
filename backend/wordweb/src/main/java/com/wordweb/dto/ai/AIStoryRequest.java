package com.wordweb.dto.ai;

import lombok.Getter;

@Getter
public class AIStoryRequest {
    private String[] words;
    private String difficulty;
    private String style;
}
