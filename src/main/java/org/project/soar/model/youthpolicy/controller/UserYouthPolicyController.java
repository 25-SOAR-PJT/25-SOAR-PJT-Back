package org.project.soar.model.youthpolicy.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.project.soar.config.TokenProvider;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.user.User;
import org.project.soar.model.user.repository.UserRepository;
import org.project.soar.model.youthpolicy.dto.YouthPolicyApplyResponseDto;
import org.project.soar.model.youthpolicy.dto.YouthPolicyBookmarkResponseDto;
import org.project.soar.model.youthpolicy.service.UserYouthPolicyService;
import org.project.soar.model.youthpolicy.service.YouthPolicyBookmarkService;
import org.springframework.http.HttpHeaders;
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

        String token = extractAccessToken(request);
        String subject = tokenProvider.validateTokenAndGetSubject(token);
        String[] parts = subject.split(":");

        if (parts.length < 1) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("잘못된 토큰입니다."));
        }

        Long userId = Long.parseLong(parts[0]);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 정보를 찾을 수 없습니다."));
        }

        String url = userYouthPolicyService.applyToPolicy(user, policyId);
        if (url == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("이미 신청한 정책이거나 종료된 정책입니다."));
        }

        YouthPolicyApplyResponseDto responseDto = new YouthPolicyApplyResponseDto(
                url,
                "정책 신청이 완료되었습니다.",
                policyId,
                user.getUserId());

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(
                responseDto,
                String.format("정책 신청 완료: policyId=%s, userId=%d, redirectUrl 반환됨", policyId, user.getUserId())));
    }

    /**
     * 정책 북마크 토글 API
     */
    @PostMapping("/{policyId}/bookmarks/toggle")
    public ResponseEntity<ApiResponse<?>> toggleBookmark(
            HttpServletRequest request,
            @PathVariable String policyId) {

        User user = getUserFromToken(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        boolean added = bookmarkService.toggleBookmark(user, policyId);

        String message = added
                ? String.format("북마크 추가됨: policyId=%s, userId=%d", policyId, user.getUserId())
                : String.format("북마크 제거됨: policyId=%s, userId=%d", policyId, user.getUserId());

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
     * 사용자 북마크 리스트 조회 API
     */
    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<?>> getBookmarks(HttpServletRequest request) {
        User user = getUserFromToken(request);
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

        User user = getUserFromToken(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        boolean bookmarked = bookmarkService.isBookmarked(user, policyId);
        String message = String.format(
                "정책 북마크 상태 조회됨: policyId=%s, bookmarked=%b", policyId, bookmarked);

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(bookmarked, message));
    }

    private String extractAccessToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private User getUserFromToken(HttpServletRequest request) {
        String token = extractAccessToken(request);
        if (token == null)
            return null;

        try {
            String subject = tokenProvider.validateTokenAndGetSubject(token);
            String[] parts = subject.split(":");
            if (parts.length < 1)
                return null;
            Long userId = Long.parseLong(parts[0]);
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
