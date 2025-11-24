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
    private Integer score;

    @Column(name = "RELATION_TYPE")
    private String relationType;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;
}

