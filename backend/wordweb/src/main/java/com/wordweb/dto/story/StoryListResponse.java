package com.wordweb.dto.story;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryListResponse {
    private Long storyId;
    private String title;
    private String titleKo;
    private String storyEn;
    private String storyKo;
    private LocalDateTime createdAt;
    private List<String> keywords;
}


