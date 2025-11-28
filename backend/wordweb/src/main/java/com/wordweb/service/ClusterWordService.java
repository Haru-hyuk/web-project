package com.wordweb.service;

import com.wordweb.entity.ClusterWord;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.repository.ClusterWordRepository;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClusterWordService {

    private final ClusterWordRepository clusterWordRepository;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;

    /** 로그인 유저 가져오기 */
    private User getLoginUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저 정보를 찾을 수 없습니다."));
    }

    /** 특정 중심 단어에 대한 클러스터 추가 */
    public void addCluster(Long centerWordId, Long relatedWordId, Double score, String type) {
        User user = getLoginUser();

        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("기준 단어를 찾을 수 없습니다."));

        Word relatedWord = wordRepository.findById(relatedWordId)
                .orElseThrow(() -> new RuntimeException("연관 단어를 찾을 수 없습니다."));

        // 중복 방지
        if (clusterWordRepository.existsByUserAndCenterWordAndRelatedWord(user, centerWord, relatedWord)) {
            return; // 이미 존재하면 아무것도 안 함 (idempotent)
        }

        clusterWordRepository.save(ClusterWord.create(user, centerWord, relatedWord, score, type));
    }

    /** 특정 중심 단어의 모든 클러스터 조회 */
    public List<ClusterWord> getCluster(Long centerWordId) {
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("기준 단어를 찾을 수 없습니다."));

        return clusterWordRepository.findByCenterWord(centerWord);
    }

    /** 유저가 가진 전체 클러스터 조회 */
    public List<ClusterWord> getMyClusters() {
        User user = getLoginUser();
        return clusterWordRepository.findByUser(user);
    }

    /** 특정 중심 단어의 클러스터를 유저 기준으로 조회 */
    public List<ClusterWord> getMyClustersByCenter(Long centerWordId) {
        User user = getLoginUser();
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("기준 단어를 찾을 수 없습니다."));
        return clusterWordRepository.findByUserAndCenterWord(user, centerWord);
    }

    /** 특정 중심 단어의 클러스터 전체 삭제 */
    public void deleteCluster(Long centerWordId) {
        Word centerWord = wordRepository.findById(centerWordId)
                .orElseThrow(() -> new RuntimeException("기준 단어를 찾을 수 없습니다."));
        clusterWordRepository.deleteByCenterWord(centerWord);
    }
}
