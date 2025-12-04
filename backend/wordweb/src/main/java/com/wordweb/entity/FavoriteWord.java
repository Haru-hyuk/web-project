package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FAVORITE_WORD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FavoriteWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FAVORITE_WORD_ID")
    private Long favoriteWordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORD_ID", nullable = false)
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public static FavoriteWord create(User user, Word word) {
        return FavoriteWord.builder()
                .user(user)
                .word(word)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
