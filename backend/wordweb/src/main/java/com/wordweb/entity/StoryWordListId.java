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
    private Long wordId;   // PK는 storyId + wordId

    public StoryWordListId(Long storyId, Long wordId) {
        this.storyId = storyId;
        this.wordId = wordId;
    }
}
