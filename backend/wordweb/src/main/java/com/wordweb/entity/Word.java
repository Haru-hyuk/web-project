package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Table(name = "WORD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word {

    @Id
    @Column(name = "WORD_ID")
    private Long wordId;

    @Column(name = "WORD", nullable = false, unique = true)
    private String word;

    @Column(name = "MEANING", nullable = false)
    private String meaning;

    @Column(name = "PART_OF_SPEECH")
    private String partOfSpeech;

    @Column(name = "EXAMPLE_SENTENCE")	
    private String exampleSentence;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "WORD_LEVEL")
    private Integer wordLevel;   // ★ 변경된 필드명

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;
}
