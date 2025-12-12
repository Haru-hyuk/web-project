package com.wordweb.dto.ai;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AIStoryResult {

    private boolean success;

    // 제목
    private String title;   // 영어 제목
    private String titleKo;   // ✅ 생성 시 함께 세팅

    // 본문
    private String storyEn;
    private String storyKo;

    private List<String> usedWords;

    // DB 저장 후
    private Long storyId;
}
