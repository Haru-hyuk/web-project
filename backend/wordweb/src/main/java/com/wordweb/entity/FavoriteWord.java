package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Table(name = "FAVORITE_WORD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteWord {

    @Id
    @Column(name = "FAVORITE_WORD_ID")
    private Long favoriteWordId;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "WORD_ID", nullable = false)
    private Word word;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;
}

