package org.project.soar.model.youthpolicy.controller;

import lombok.RequiredArgsConstructor;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.user.User;
import org.project.soar.model.user.repository.UserRepository;
import org.project.soar.model.youthpolicy.dto.YouthPolicyApplyResponseDto;
import org.project.soar.model.youthpolicy.service.UserYouthPolicyService;
import org.project.soar.config.TokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-policies")
public class UserYouthPolicyController {

    private final UserYouthPolicyService userYouthPolicyService;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/{policyId}/apply")
    public ResponseEntity<ApiResponse<?>> applyToPolicy(
            @PathVariable String policyId,
            HttpServletRequest request) {

        // 1. JWT 토큰에서 사용자 ID 추출
        String token = extractAccessToken(request);
        String subject = tokenProvider.validateTokenAndGetSubject(token); // e.g., "3:ROLE_USER"
        String[] parts = subject.split(":");

        if (parts.length < 1) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("잘못된 토큰입니다."));
        }

        Long userId = Long.parseLong(parts[0]);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 정보를 찾을 수 없습니다."));
        }

        // 2. 신청 시도
        String url = userYouthPolicyService.applyToPolicy(user, policyId);

        if (url == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("이미 신청한 정책이거나 종료된 정책입니다."));
        }

        // 3. 응답 DTO 반환
        YouthPolicyApplyResponseDto responseDto = new YouthPolicyApplyResponseDto(
                url,
                "정책 신청이 완료되었습니다.",
                policyId,
                user.getUserId());

        return ResponseEntity.ok(ApiResponse.createSuccess(responseDto));
    }

    private String extractAccessToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
