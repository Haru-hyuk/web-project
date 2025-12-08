package com.wordweb.controller;

import com.wordweb.dto.favorite.FavoriteWordResponse;
import com.wordweb.service.FavoriteWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteWordController {

    private final FavoriteWordService favoriteWordService;

    /** 즐겨찾기 추가 */
    @PostMapping("/{wordId}")
    public ResponseEntity<Void> addFavorite(@PathVariable Long wordId) {
        favoriteWordService.addFavorite(wordId);
        return ResponseEntity.created(URI.create("/api/favorites/" + wordId)).build(); // 201
    }

    /** 즐겨찾기 삭제 */
    @DeleteMapping("/{wordId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long wordId) {
        favoriteWordService.removeFavorite(wordId);
        return ResponseEntity.noContent().build(); // 204
    }

    /** 즐겨찾기 목록 조회 */
    @GetMapping
    public ResponseEntity<List<FavoriteWordResponse>> getMyFavorites() {
        return ResponseEntity.ok(favoriteWordService.getMyFavorites());
    }
}
