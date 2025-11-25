package com.wordweb.controller;

import com.wordweb.dto.favorite.FavoriteWordResponse;
import com.wordweb.entity.FavoriteWord;
import com.wordweb.service.FavoriteWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorite")
public class FavoriteWordController {

    private final FavoriteWordService favoriteWordService;

    /** 즐겨찾기 추가 */
    @PostMapping("/{wordId}")
    public ResponseEntity<String> addFavorite(@PathVariable Long wordId) {
        favoriteWordService.addFavorite(wordId);
        return ResponseEntity.ok("즐겨찾기에 추가되었습니다.");
    }

    /** 즐겨찾기 삭제 */
    @DeleteMapping("/{wordId}")
    public ResponseEntity<String> removeFavorite(@PathVariable Long wordId) {
        favoriteWordService.removeFavorite(wordId);
        return ResponseEntity.ok("즐겨찾기가 삭제되었습니다.");
    }

    /** 즐겨찾기 목록 조회 */
    @GetMapping
    public ResponseEntity<List<FavoriteWordResponse>> getMyFavorites() {
        List<FavoriteWord> list = favoriteWordService.getMyFavorites();
        return ResponseEntity.ok(
                list.stream().map(FavoriteWordResponse::from).toList()
        );
    }
}
