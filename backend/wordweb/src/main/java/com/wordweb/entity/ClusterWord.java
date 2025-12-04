package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLUSTER_WORD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ClusterWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLUSTER_ID")
    private Long clusterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CENTER_WORD_ID", nullable = false)
    private Word centerWord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RELATED_WORD_ID", nullable = false)
    private Word relatedWord;

    @Column(name = "SCORE")
    private Double score;

    @Column(name = "RELATION_TYPE")
    private String relationType;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public static ClusterWord create(User user, Word center, Word related, double score, String relationType) {
        return ClusterWord.builder()
                .user(user)
                .centerWord(center)
                .relatedWord(related)
                .score(score)
                .relationType(relationType)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
