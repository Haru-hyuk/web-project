package com.wordweb.service;

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

    // 현재 로그인 유저 불러오기
    private User getLoginUser() {
        String email = SecurityUtil.getLoginUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    /** 즐겨찾기 추가 */
    public void addFavorite(Long wordId) {

        User user = getLoginUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다."));

        // 이미 등록된 즐겨찾기인지 체크
        if (favoriteWordRepository.existsByUserAndWord(user, word)) {
            throw new RuntimeException("이미 즐겨찾기한 단어입니다.");
        }

        FavoriteWord fw = FavoriteWord.builder()
                .user(user)
                .word(word)
                .build();

        favoriteWordRepository.save(fw);
    }

    /** 즐겨찾기 삭제 */
    public void removeFavorite(Long wordId) {
        User user = getLoginUser();

        favoriteWordRepository.deleteByUserEmailAndWordWordId(user.getEmail(), wordId);
    }

    /** 즐겨찾기 목록 조회 */
    public List<FavoriteWord> getMyFavorites() {
        User user = getLoginUser();
        return favoriteWordRepository.findByUser(user);
    }
}
