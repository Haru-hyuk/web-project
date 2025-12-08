package com.wordweb.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIStoryResponse {
    private boolean success;
    private String message;
    private String title;  // AI 생성 제목
    private String storyEn;
    private String storyKo;
    private List<String> usedWords;
    private Long storyId;  // 생성된 스토리 ID
}
