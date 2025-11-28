package com.wordweb.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class StoryWordListId implements Serializable {

    private Long storyId;
    private Long wrongWordId;

    public StoryWordListId(Long storyId, Long wrongWordId) {
        this.storyId = storyId;
        this.wrongWordId = wrongWordId;
    }
}
