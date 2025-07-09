package org.project.soar.model.youthpolicy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.soar.global.scheduler.YouthPolicyScheduler;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicyStep;
import org.project.soar.model.youthpolicy.repository.YouthPolicyStepRepository;
import org.project.soar.model.youthpolicy.service.YouthPolicyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/youth-policy")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class YouthPolicyController {

    private final YouthPolicyService youthPolicyService;
    private final YouthPolicyScheduler youthPolicyScheduler;
    private final YouthPolicyStepRepository youthPolicyStepRepository;

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncYouthPolicies() {
        try {
            youthPolicyScheduler.manualSync();
            long totalCount = youthPolicyService.getTotalCount();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "청년정책 데이터 동기화가 완료되었습니다.",
                    "totalCount", totalCount,
                    "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("데이터 동기화 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "데이터 동기화에 실패했습니다: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        }
    }
    
    @GetMapping("/step/{policyId}")
    public ResponseEntity<YouthPolicyStep> getStepByPolicyId(@PathVariable String policyId) {
        return ResponseEntity.of(youthPolicyStepRepository.findByPolicyId(policyId));
    }


    /**
     * 전체 청년정책 목록 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllYouthPolicies(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String category) {

        try {
            List<YouthPolicy> policies;

            if (!keyword.isEmpty()) {
                policies = youthPolicyService.searchByKeyword(keyword);
            } else if (!category.isEmpty()) {
                policies = youthPolicyService.findByCategory(category);
            } else {
                policies = youthPolicyService.getAllYouthPolicies();
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", policies,
                    "count", policies.size(),
                    "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("청년정책 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "데이터 조회에 실패했습니다: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        }
    }

    /**
     * 키워드로 청년정책 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<YouthPolicy>> searchByKeyword(@RequestParam("keyword") String keyword) {
        List<YouthPolicy> result = youthPolicyService.searchPolicies(keyword);
        return ResponseEntity.ok(result);
    }

    /**
     * 페이징된 청년정책 목록 조회
     */
    @GetMapping("/paged")
    public ResponseEntity<Map<String, Object>> getYouthPoliciesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<YouthPolicy> policyPage = youthPolicyService.getYouthPoliciesPaged(pageable);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", policyPage.getContent(),
                    "totalElements", policyPage.getTotalElements(),
                    "totalPages", policyPage.getTotalPages(),
                    "currentPage", page,
                    "size", size,
                    "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("페이징된 청년정책 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "데이터 조회에 실패했습니다: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        }
    }

    /**
     * 특정 청년정책 상세 조회
     */
    @GetMapping("/{policyId}")
    public ResponseEntity<Map<String, Object>> getYouthPolicyById(@PathVariable String policyId) {
        try {
            YouthPolicy policy = youthPolicyService.getYouthPolicyById(policyId);

            if (policy != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", policy,
                        "timestamp", System.currentTimeMillis()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("청년정책 상세 조회 실패: {}", policyId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "데이터 조회에 실패했습니다: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        }
    }

    /**
     * 현재 신청 가능한 청년정책 조회
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentYouthPolicies() {
        try {
            List<YouthPolicy> currentPolicies = youthPolicyService.getCurrentAvailablePolicies();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", currentPolicies,
                    "count", currentPolicies.size(),
                    "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("현재 신청 가능한 청년정책 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "데이터 조회에 실패했습니다: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        }
    }

    /**
     * 청년정책 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getYouthPolicyStats() {
        try {
            Map<String, Object> stats = youthPolicyService.getStatistics();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", stats,
                    "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("청년정책 통계 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "통계 데이터 조회에 실패했습니다: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        }
    }

    /**
     * 데이터베이스 상태 확인
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            long totalCount = youthPolicyService.getTotalCount();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "status", "healthy",
                    "totalPolicies", totalCount,
                    "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("헬스체크 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "status", "unhealthy",
                    "message", e.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        }
    }
}