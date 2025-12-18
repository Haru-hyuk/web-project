package com.wordweb.repository;

import com.wordweb.entity.ClusterWord;
import com.wordweb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClusterWordRepository extends JpaRepository<ClusterWord, Long> {

    /** 중심 단어 기준 클러스터 조회 */
    List<ClusterWord> findByCenterWord(Word centerWord);

    /** 중심 단어 + 관련 단어 조합이 이미 존재하는지 체크 */
    boolean existsByCenterWordAndRelatedWord(Word centerWord, Word relatedWord);

    /** 중심 단어 삭제 시 관련 클러스터 전부 삭제할 때 */
    void deleteByCenterWord(Word centerWord);
}
