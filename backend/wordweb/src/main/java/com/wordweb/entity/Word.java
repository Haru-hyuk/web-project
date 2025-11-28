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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "word_seq")
    @SequenceGenerator(name = "word_seq", sequenceName = "SEQ_WORD_ID", allocationSize = 1)
    @Column(name = "WORD_ID")
    private Long wordId;

    @Column(name = "WORD", nullable = false, unique = true)
    private String word;

    @Column(name = "MEANING", nullable = false)
    private String meaning;

    @Column(name = "PART_OF_SPEECH")
    private String partOfSpeech;

    @Lob
    @Column(name = "EXAMPLE_SENTENCE_EN")
    private String exampleSentenceEn;

    @Lob
    @Column(name = "EXAMPLE_SENTENCE_KO")
    private String exampleSentenceKo;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "LEVEL")
    private Integer level;

    @Lob
    @Column(name = "EMBEDDING")
    private String embedding;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;
}
