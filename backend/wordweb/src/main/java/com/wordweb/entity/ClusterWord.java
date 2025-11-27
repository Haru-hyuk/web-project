package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Table(name = "CLUSTER_WORD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClusterWord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cluster_seq_gen")
    @SequenceGenerator(name = "cluster_seq_gen", sequenceName = "SEQ_CLUSTER_ID", allocationSize = 1)
    @Column(name = "CLUSTER_ID")
    private Long clusterId;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "RELATED_WORD_ID", nullable = false)
    private Word relatedWord;

    @ManyToOne
    @JoinColumn(name = "CENTER_WORD_ID", nullable = false)
    private Word centerWord;

    @Column(name = "SCORE")
    private Double score;

    @Column(name = "RELATION_TYPE")
    private String relationType;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;
}

