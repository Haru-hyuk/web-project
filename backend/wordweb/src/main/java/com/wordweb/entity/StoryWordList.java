package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "STORY_WORD_LIST")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryWordList {

    @Id
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "STORY_ID", nullable = false)
    private Story story;

    @ManyToOne
    @JoinColumn(name = "WRONG_WORD_ID", nullable = false)
    private WrongWord wrongWord;
}
