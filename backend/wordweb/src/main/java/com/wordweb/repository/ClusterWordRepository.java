package com.wordweb.repository;

import com.wordweb.entity.ClusterWord;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClusterWordRepository extends JpaRepository<ClusterWord, Long> {

    // 특정 사용자의 특정 중심 단어에 대한 클러스터 조회
    List<ClusterWord> findByUserAndCenterWord(User user, Word centerWord);

    // 특정 사용자의 모든 클러스터 조회
    List<ClusterWord> findByUser(User user);
}
