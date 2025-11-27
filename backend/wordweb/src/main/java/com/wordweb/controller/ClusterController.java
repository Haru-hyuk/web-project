package com.wordweb.controller;

import com.wordweb.entity.ClusterWord;
import com.wordweb.entity.User;
import com.wordweb.entity.Word;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.security.SecurityUtil;
import com.wordweb.service.ClusterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 간단한 클러스터링 API
 * 기존 구조 활용
 */
@RestController
@RequestMapping("/api/cluster")
@RequiredArgsConstructor
public class ClusterController {

    private final ClusterService clusterService;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;

    /**
     * 클러스터 생성
     * POST /api/cluster/create?wordId=1&topN=10&threshold=0.5
     */
    @PostMapping("/create")
    public ResponseEntity<?> createCluster(
            @RequestParam("wordId") Long wordId,
            @RequestParam(value = "topN", defaultValue = "10") int topN,
            @RequestParam(value = "threshold", defaultValue = "0.5") double threshold
    ) {
        try {
            String email = SecurityUtil.getCurrentUserEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            List<ClusterWord> clusters = clusterService.createCluster(user, wordId, topN, threshold);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", clusters.size());
            response.put("message", "클러스터 생성 완료");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 클러스터 조회
     * GET /api/cluster?wordId=1
     */
    @GetMapping
    public ResponseEntity<?> getClusters(@RequestParam("wordId") Long wordId) {
        try {
            String email = SecurityUtil.getCurrentUserEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            Word centerWord = wordRepository.findById(wordId)
                    .orElseThrow(() -> new RuntimeException("단어를 찾을 수 없습니다"));

            List<ClusterWord> clusters = clusterService.getClusters(user, centerWord);
            return ResponseEntity.ok(clusters);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 모든 클러스터 조회
     * GET /api/cluster/all
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllClusters() {
        try {
            String email = SecurityUtil.getCurrentUserEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            List<ClusterWord> clusters = clusterService.getAllClusters(user);
            return ResponseEntity.ok(clusters);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
