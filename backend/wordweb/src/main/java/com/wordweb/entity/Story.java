package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Table(name = "STORY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Story {

    @Id
    @Column(name = "STORY_ID")
    private Long storyId;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "STORY_EN")
    private String storyEn;

    @Column(name = "STORY_KO")
    private String storyKo;

    @Column(name = "TARGET_WORD_IDS")
    private String targetWordIds;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;
}

