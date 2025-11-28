package com.wordweb.service;

import com.wordweb.dto.favorite.FavoriteWordResponse;
import com.wordweb.entity.FavoriteWord;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.repository.FavoriteWordRepository;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteWordService {

    private final FavoriteWordRepository favoriteWordRepository;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;

    /** 현재 로그인 유저 가져오기 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저를 찾을 수 없습니다."));
    }

    /** 즐겨찾기 추가 */
    public void addFavorite(Long wordId) {
        User user = getLoginUser();

        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        if (favoriteWordRepository.existsByUserAndWord(user, word)) {
            throw new RuntimeException("이미 즐겨찾기한 단어입니다.");
        }

        favoriteWordRepository.save(FavoriteWord.create(user, word));
    }

    /** 즐겨찾기 삭제 */
    public void removeFavorite(Long wordId) {
        User user = getLoginUser();

        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        favoriteWordRepository.deleteByUserAndWord(user, word);
    }

    /** 내 즐겨찾기 목록 조회 → DTO로 변환 */
    public List<FavoriteWordResponse> getMyFavorites() {
        User user = getLoginUser();

        return favoriteWordRepository.findByUser(user)
                .stream()
                .map(FavoriteWordResponse::from) // ← DTO 변환
                .toList();
    }
}
