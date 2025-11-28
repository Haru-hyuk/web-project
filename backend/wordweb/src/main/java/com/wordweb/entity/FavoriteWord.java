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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "favorite_seq")
    @SequenceGenerator(name = "favorite_seq", sequenceName = "SEQ_FAVORITE_WORD_ID", allocationSize = 1)
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

    /** 정적 팩토리 메서드로 생성 */
    public static FavoriteWord create(User user, Word word) {
        return FavoriteWord.builder()
                .user(user)
                .word(word)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
