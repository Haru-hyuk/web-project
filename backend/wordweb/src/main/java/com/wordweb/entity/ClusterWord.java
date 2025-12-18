package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "CLUSTER_WORD",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_CLUSTER_WORD_CENTER_RELATED",
            columnNames = {"CENTER_WORD_ID", "RELATED_WORD_ID"}
        )
    }
)
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
    @JoinColumn(name = "CENTER_WORD_ID", nullable = false)
    private Word centerWord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RELATED_WORD_ID", nullable = false)
    private Word relatedWord;

    @Column(name = "SCORE")
    private Double score;

    @Column(name = "RELATION_TYPE")
    private String relationType;   // 예: synonym, antonym, related

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** 클러스터 데이터 생성 */
    public static ClusterWord create(Word center, Word related, double score, String relationType) {
        return ClusterWord.builder()
                .centerWord(center)
                .relatedWord(related)
                .score(score)
                .relationType(relationType)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
