package com.wordweb.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AIStoryResponse {
    private boolean success;
    private String message;
    private String storyEn;
    private String storyKo;
    private List<String> usedWords;
}
