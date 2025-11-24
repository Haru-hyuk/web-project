package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Table(name = "WRONG_WORD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrongWord {

    @Id
    @Column(name = "WRONG_WORD_ID")
    private Long wrongWordId;

    @ManyToOne
    @JoinColumn(name = "WORD_ID", nullable = false)
    private Word word;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "WRONG_AT")
    private Timestamp wrongAt;

    @Column(name = "TAG")
    private String tag;

    @Column(name = "IS_USED_IN_STORY")
    private String isUsedInStory;
}
