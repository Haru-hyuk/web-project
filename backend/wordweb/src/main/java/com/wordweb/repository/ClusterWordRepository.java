package com.wordweb.repository;

import com.wordweb.entity.ClusterWord;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClusterWordRepository extends JpaRepository<ClusterWord, Long> {

    /** 유저가 생성한 모든 클러스터 */
    List<ClusterWord> findByUser(User user);

    /** 중심 단어 기준 클러스터 조회 */
    List<ClusterWord> findByCenterWord(Word centerWord);

    /** 유저 + 중심 단어 기준 조회 */
    List<ClusterWord> findByUserAndCenterWord(User user, Word centerWord);

    /** 유저 + 중심 단어 + 관련 단어 조합이 이미 존재하는지 체크 */
    boolean existsByUserAndCenterWordAndRelatedWord(User user, Word centerWord, Word relatedWord);

    /** 중심 단어 삭제 시 관련 클러스터 전부 삭제할 때 */
    void deleteByCenterWord(Word centerWord);

    /** 유저 + 중심 단어 기준으로 클러스터 삭제 */
    void deleteByUserAndCenterWord(User user, Word centerWord);

    /** 유저의 모든 클러스터 삭제 */
    void deleteByUser(User user);
}
