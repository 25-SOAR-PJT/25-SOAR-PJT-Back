package org.project.soar.model.youthpolicy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.soar.global.api.ApiResponse;
import org.project.soar.global.scheduler.YouthPolicyScheduler;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicyStep;
import org.project.soar.model.youthpolicy.repository.YouthPolicyStepRepository;
import org.project.soar.model.youthpolicy.service.YouthPolicyService;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse<?>> syncYouthPolicies() {
        try {
            youthPolicyScheduler.manualSync();
            long totalCount = youthPolicyService.getTotalCount();

            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(totalCount, "청년정책 데이터 동기화가 완료되었습니다."));
        } catch (Exception e) {
            log.error("데이터 동기화 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.createError("데이터 동기화 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/step/{policyId}")
    public ResponseEntity<ApiResponse<?>> getStepByPolicyId(@PathVariable String policyId) {
        try {
            List<YouthPolicyStep> steps = youthPolicyStepRepository.findAllByPolicyId(policyId);
            if (steps.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null, "해당 정책에 대한 단계 정보가 없습니다."));
            }
            return ResponseEntity.ok(ApiResponse.createSuccess(steps.get(steps.size() - 1)));
        } catch (Exception e) {
            log.error("정책 단계 조회 실패: {}", policyId, e);
            return ResponseEntity.internalServerError().body(ApiResponse.createError("단계 조회 실패: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllYouthPolicies(
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

            return ResponseEntity.ok(ApiResponse.createSuccess(policies));
        } catch (Exception e) {
            log.error("청년정책 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.createError("데이터 조회 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchByKeyword(@RequestParam("keyword") String keyword) {
        try {
            List<YouthPolicy> result = youthPolicyService.searchPolicies(keyword);
            return ResponseEntity.ok(ApiResponse.createSuccess(result));
        } catch (Exception e) {
            log.error("정책 키워드 검색 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.createError("검색 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<?>> getYouthPoliciesPaged(
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

            return ResponseEntity.ok(ApiResponse.createSuccess(policyPage));
        } catch (Exception e) {
            log.error("페이징 정책 조회 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.createError("페이징 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<ApiResponse<?>> getYouthPolicyById(@PathVariable String policyId) {
        try {
            YouthPolicy policy = youthPolicyService.getYouthPolicyById(policyId);

            if (policy != null) {
                return ResponseEntity.ok(ApiResponse.createSuccess(policy));
            } else {
                return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null, "정책을 찾을 수 없습니다."));
            }
        } catch (Exception e) {
            log.error("정책 상세 조회 실패: {}", policyId, e);
            return ResponseEntity.internalServerError().body(ApiResponse.createError("정책 조회 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<?>> getCurrentYouthPolicies() {
        try {
            List<YouthPolicy> currentPolicies = youthPolicyService.getCurrentAvailablePolicies();
            return ResponseEntity.ok(ApiResponse.createSuccess(currentPolicies));
        } catch (Exception e) {
            log.error("현재 신청 가능한 정책 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.createError("신청 가능 정책 조회 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getYouthPolicyStats() {
        try {
            return ResponseEntity.ok(ApiResponse.createSuccess(youthPolicyService.getStatistics()));
        } catch (Exception e) {
            log.error("정책 통계 조회 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.createError("정책 통계 조회 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<?>> healthCheck() {
        try {
            long totalCount = youthPolicyService.getTotalCount();
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(totalCount, "DB 연결 정상"));
        } catch (Exception e) {
            log.error("헬스 체크 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.createError("헬스 체크 실패: " + e.getMessage()));
        }
    }

    // 마감임박 지원사업 리스트
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<?>> getLatestPoliciesByEndDate() {
        try {
            Page<YouthPolicy> policies = youthPolicyService.getLatestPoliciesByEndDate();

            return ResponseEntity.ok(ApiResponse.createSuccess(policies));
        } catch (Exception e) {
            log.error("마감임박 지원사업 조회 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.createError("마감임박 지원사업 조회 실패: " + e.getMessage()));
        }
    }

}
