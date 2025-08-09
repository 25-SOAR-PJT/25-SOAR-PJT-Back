package org.project.soar.model.youthpolicy.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.project.soar.config.TokenProvider;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.user.User;
import org.project.soar.model.user.repository.UserRepository;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.dto.YouthPolicyApplyResponseDto;
import org.project.soar.model.youthpolicy.dto.YouthPolicyBookmarkResponseDto;
import org.project.soar.model.youthpolicy.dto.YouthPolicyLatestResponseDto;
import org.project.soar.model.youthpolicy.service.UserYouthPolicyService;
import org.project.soar.model.youthpolicy.service.YouthPolicyBookmarkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-policies")
public class UserYouthPolicyController {

    private final UserYouthPolicyService userYouthPolicyService;
    private final YouthPolicyBookmarkService bookmarkService;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    /**
     * 정책 신청 API
     */
    @PostMapping("/{policyId}/apply")
    public ResponseEntity<ApiResponse<?>> applyToPolicy(
            @PathVariable String policyId,
            HttpServletRequest request) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        YouthPolicyApplyResponseDto dto = userYouthPolicyService.applyToPolicy(user, policyId);

        // 종료/마감/이미 신청 → 에러 처리
        String msg = dto.getMessage() == null ? "" : dto.getMessage();
        boolean isError = msg.contains("종료") || msg.contains("마감") || msg.contains("이미 신청");
        if (isError) {
            return ResponseEntity.badRequest().body(ApiResponse.createError(msg));
        }

        // 정상 신청 완료 (applyUrl 없어도 성공으로 처리)
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(
                dto,
                (dto.getApplyUrl() == null || dto.getApplyUrl().isBlank())
                        ? String.format("정책 신청 완료(이동할 URL 없음): policyId=%s, userId=%d",
                                dto.getPolicyId(), dto.getUserId())
                        : String.format("정책 신청 완료: policyId=%s, userId=%d, redirectUrl 반환됨",
                                dto.getPolicyId(), dto.getUserId())));
    }

    /**
     * 정책 북마크 토글 API
     */
    @PostMapping("/{policyId}/bookmarks/toggle")
    public ResponseEntity<ApiResponse<?>> toggleBookmark(
            HttpServletRequest request,
            @PathVariable String policyId) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        boolean added = bookmarkService.toggleBookmark(user, policyId);

        String message = added
                ? String.format("북마크 추가됨: policyId=%s, userId=%d", policyId, user.getUserId())
                : String.format("북마크 해제됨: policyId=%s, userId=%d", policyId, user.getUserId());

        // 북마크 추가 시에만 DTO 반환
        if (added) {
            YouthPolicyBookmarkResponseDto dto = bookmarkService.getUserBookmarkDtos(user).stream()
                    .filter(b -> b.getPolicyId().equals(policyId))
                    .findFirst()
                    .orElse(null);
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(dto, message));
        } else {
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null, message));
        }
    }

    /**
     * 특정 정책 북마크 해제 API (idempotent)
     */
    @DeleteMapping("/{policyId}/bookmarks")
    public ResponseEntity<ApiResponse<?>> unbookmarkPolicy(
            HttpServletRequest request,
            @PathVariable String policyId) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        bookmarkService.unbookmark(user, policyId);
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(
                null,
                String.format("북마크 해제 완료: policyId=%s, userId=%d", policyId, user.getUserId())));
    }

    /**
     * 모든 정책 북마크 해제 API (전체 삭제, idempotent)
     */
    @DeleteMapping("/bookmarks")
    public ResponseEntity<ApiResponse<?>> unbookmarkAllPolicies(HttpServletRequest request) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        bookmarkService.unbookmarkAll(user);
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(
                null,
                String.format("모든 북마크 해제 완료: userId=%d", user.getUserId())));
    }

    /**
     * 사용자 북마크 리스트 조회 API
     */
    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<?>> getBookmarks(HttpServletRequest request) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        List<YouthPolicyBookmarkResponseDto> bookmarkDtos = bookmarkService.getUserBookmarkDtos(user);
        String message = String.format(
                "북마크된 정책 총 %d건 조회됨: policyId, policyName, dateLabel 필드 포함",
                bookmarkDtos.size());

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(bookmarkDtos, message));
    }

    /**
     * 특정 정책 북마크 여부 조회 API
     */
    @GetMapping("/{policyId}/bookmarks/status")
    public ResponseEntity<ApiResponse<?>> isBookmarked(
            HttpServletRequest request,
            @PathVariable String policyId) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        boolean bookmarked = bookmarkService.isBookmarked(user, policyId);
        String message = String.format(
                "정책 북마크 상태 조회됨: policyId=%s, bookmarked=%b", policyId, bookmarked);

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(bookmarked, message));
    }

    /**
     * 북마크된 지원사업 중 가장 종료일이 가까운 정책 조회 API
     */
    @GetMapping("/bookmarks/latest/{userId}")
    public ResponseEntity<ApiResponse<YouthPolicyLatestResponseDto>> getLatestBookmarkByEndDate(
            @PathVariable Long userId) {

        YouthPolicyLatestResponseDto youthPolicy = bookmarkService.getLatestBookmarkByEndDate(userId);
        if (youthPolicy == null) {
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null, "북마크된 정책이 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(youthPolicy, "가장 최근 종료일이 가까운 정책 조회됨"));
    }

    /**
     * 인기 정책 조회 API (북마크 기준)
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<?>> getPopularPolicies() {
        try {
            List<YouthPolicy> popularPolicies = bookmarkService.getPopularPolicies();
            return ResponseEntity.ok(ApiResponse.createSuccess(popularPolicies));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.createError("인기 정책 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 나이대별 인기 정책 조회 API (북마크 기준)
     */
    @GetMapping("/popular/age-group/{userId}")
    public ResponseEntity<ApiResponse<?>> getPopularPoliciesAge(@PathVariable Long userId) {
        try {
            List<YouthPolicy> popularPolicies = bookmarkService.getPopularPoliciesAge(userId);
            return ResponseEntity.ok(ApiResponse.createSuccess(popularPolicies));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.createError("인기 정책 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 신청한 정책 개수 API
     */
    @GetMapping("/applied/count/{userId}")
    public ResponseEntity<ApiResponse<?>> getAppliedPolicyCount(@PathVariable Long userId) {
        int count = userYouthPolicyService.getAppliedPolicyCount(userId);
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(count, "신청한 정책 개수 조회됨"));
    }
}
