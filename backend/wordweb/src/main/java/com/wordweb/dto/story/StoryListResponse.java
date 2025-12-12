package com.wordweb.dto.story;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryListResponse {

    private Long storyId;

    private String title;    // 영어 제목
    private String titleKo;  // ⭐ 영어 제목 번역

    private String storyEn;
    private String storyKo;

    private LocalDateTime createdAt;
    private List<String> keywords;
}
