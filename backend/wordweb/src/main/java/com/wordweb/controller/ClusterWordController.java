package com.wordweb.controller;

import com.wordweb.entity.ClusterWord;
import com.wordweb.service.ClusterWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cluster")
@RequiredArgsConstructor
public class ClusterWordController {

    private final ClusterWordService clusterWordService;

    /**
     * 클러스터 생성
     * POST /api/cluster/create?wordId=1
     */
    @PostMapping("/create")
    public ResponseEntity<?> createCluster(@RequestParam("wordId") Long wordId) {
        try {
            List<ClusterWord> clusters = clusterWordService.createCluster(wordId);

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
            List<ClusterWord> clusters = clusterWordService.getMyClustersByCenter(wordId);
            // 클러스터가 없어도 빈 배열 반환 (정상)
            return ResponseEntity.ok(clusters);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "클러스터 조회 중 오류가 발생했습니다: " + e.getMessage());
            error.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 모든 클러스터 조회
     * GET /api/cluster/all
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllClusters() {
        try {
            List<ClusterWord> clusters = clusterWordService.getMyClusters();
            return ResponseEntity.ok(clusters);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 특정 중심 단어의 클러스터 삭제
     * DELETE /api/cluster?wordId=1
     */
    @DeleteMapping
    public ResponseEntity<?> deleteCluster(@RequestParam("wordId") Long wordId) {
        try {
            clusterWordService.deleteCluster(wordId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "클러스터가 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 사용자의 모든 클러스터 삭제
     * DELETE /api/cluster/all
     */
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllClusters() {
        try {
            clusterWordService.deleteAllClusters();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "모든 클러스터가 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

}
