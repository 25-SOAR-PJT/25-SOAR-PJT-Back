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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-policies")
@Tag(name = "UserYouthPolicy", description = "사용자 정책 신청/북마크 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class UserYouthPolicyController {

    private final UserYouthPolicyService userYouthPolicyService;
    private final YouthPolicyBookmarkService bookmarkService;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Operation(summary = "정책 신청", description = "로그인한 사용자가 특정 정책(policyId)에 신청합니다. "
            + "신청 마감/종료/이미 신청한 경우 에러 반환.")
    @PostMapping("/{policyId}/apply")
    public ResponseEntity<ApiResponse<?>> applyToPolicy(
            @Parameter(description = "정책 ID") @PathVariable String policyId,
            HttpServletRequest request) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        YouthPolicyApplyResponseDto dto = userYouthPolicyService.applyToPolicy(user, policyId);

        String msg = dto.getMessage() == null ? "" : dto.getMessage();
        boolean isError = msg.contains("종료") || msg.contains("마감") || msg.contains("이미 신청");
        if (isError) {
            return ResponseEntity.badRequest().body(ApiResponse.createError(msg));
        }

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(
                dto,
                (dto.getApplyUrl() == null || dto.getApplyUrl().isBlank())
                        ? String.format("정책 신청 완료(이동할 URL 없음): policyId=%s, userId=%d",
                                dto.getPolicyId(), dto.getUserId())
                        : String.format("정책 신청 완료: policyId=%s, userId=%d, redirectUrl 반환됨",
                                dto.getPolicyId(), dto.getUserId())));
    }

    @Operation(summary = "정책 북마크 토글", description = "해당 정책의 북마크 상태를 토글합니다. 없으면 추가, 있으면 해제.")
    @PostMapping("/{policyId}/bookmarks/toggle")
    public ResponseEntity<ApiResponse<?>> toggleBookmark(
            HttpServletRequest request,
            @Parameter(description = "정책 ID") @PathVariable String policyId) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        Boolean added = bookmarkService.toggleBookmark(user, policyId);
        if (added == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("정책을 찾을 수 없습니다."));
        }

        String message = added
                ? String.format("북마크 추가됨: policyId=%s, userId=%d", policyId, user.getUserId())
                : String.format("북마크 해제됨: policyId=%s, userId=%d", policyId, user.getUserId());

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

    @Operation(summary = "특정 정책 북마크 해제", description = "해당 정책의 북마크를 해제합니다. 이미 해제된 경우에도 성공 처리.")
    @DeleteMapping("/{policyId}/bookmarks")
    public ResponseEntity<ApiResponse<?>> unbookmarkPolicy(
            HttpServletRequest request,
            @Parameter(description = "정책 ID") @PathVariable String policyId) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        bookmarkService.unbookmark(user, policyId);
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(
                null,
                String.format("북마크 해제 완료: policyId=%s, userId=%d", policyId, user.getUserId())));
    }

    @Operation(summary = "모든 정책 북마크 해제", description = "사용자의 모든 정책 북마크를 삭제합니다. 이미 비어 있어도 성공 처리.")
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

    @Operation(summary = "사용자 북마크 리스트 조회", description = "사용자가 북마크한 모든 정책을 조회합니다.")
    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<?>> getBookmarks(HttpServletRequest request) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        List<YouthPolicyBookmarkResponseDto> bookmarkDtos = bookmarkService.getUserBookmarkDtos(user);
        String message = String.format(
                "북마크된 정책 총 %d건 조회됨", bookmarkDtos.size());

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(bookmarkDtos, message));
    }

    @Operation(summary = "특정 정책 북마크 여부 조회", description = "특정 정책이 북마크되어 있는지 여부를 반환합니다.")
    @GetMapping("/{policyId}/bookmarks/status")
    public ResponseEntity<ApiResponse<?>> isBookmarked(
            HttpServletRequest request,
            @Parameter(description = "정책 ID") @PathVariable String policyId) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        Boolean bookmarked = bookmarkService.isBookmarked(user, policyId);
        if (bookmarked == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("정책을 찾을 수 없습니다."));
        }
        String message = String.format(
                "정책 북마크 상태 조회됨: policyId=%s, bookmarked=%b", policyId, bookmarked);

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(bookmarked, message));
    }

    @Operation(summary = "북마크 중 종료일이 가장 가까운 정책 조회", description = "사용자의 북마크 중 종료일이 가장 가까운 정책을 조회합니다.")
    @GetMapping("/bookmarks/latest/{userId}")
    public ResponseEntity<ApiResponse<YouthPolicyLatestResponseDto>> getLatestBookmarkByEndDate(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {

        YouthPolicyLatestResponseDto youthPolicy = bookmarkService.getLatestBookmarkByEndDate(userId);
        if (youthPolicy == null) {
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null, "북마크된 정책이 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(youthPolicy, "가장 최근 종료일이 가까운 정책 조회됨"));
    }

    @Operation(summary = "인기 정책 조회", description = "전체 사용자 북마크 수를 기준으로 인기 정책을 조회합니다.")
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<?>> getPopularPolicies() {
        try {
            List<YouthPolicy> popularPolicies = bookmarkService.getPopularPolicies();
            return ResponseEntity.ok(ApiResponse.createSuccess(popularPolicies));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.createError("인기 정책 조회 실패: " + e.getMessage()));
        }
    }

    @Operation(summary = "나이대별 인기 정책 조회", description = "사용자의 나이대 기준 인기 정책을 조회합니다.")
    @GetMapping("/popular/age-group/{userId}")
    public ResponseEntity<ApiResponse<?>> getPopularPoliciesAge(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        try {
            List<YouthPolicy> popularPolicies = bookmarkService.getPopularPoliciesAge(userId);
            return ResponseEntity.ok(ApiResponse.createSuccess(popularPolicies));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.createError("인기 정책 조회 실패: " + e.getMessage()));
        }
    }

    @Operation(summary = "신청한 정책 개수 조회", description = "사용자가 신청한 정책 개수를 조회합니다.")
    @GetMapping("/applied/count/{userId}")
    public ResponseEntity<ApiResponse<?>> getAppliedPolicyCount(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        int count = userYouthPolicyService.getAppliedPolicyCount(userId);
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(count, "신청한 정책 개수 조회됨"));
    }
}
