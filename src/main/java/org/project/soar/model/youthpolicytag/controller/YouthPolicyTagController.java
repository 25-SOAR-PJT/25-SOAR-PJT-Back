package org.project.soar.model.youthpolicytag.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.config.TokenProvider;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.tag.dto.YouthPolicyTagsResponse;
import org.project.soar.model.user.User;
import org.project.soar.model.usertag.service.UserTagService;
import org.project.soar.model.youthpolicytag.dto.FindYouthPolicyByTagResponse;
import org.project.soar.model.youthpolicytag.dto.YouthPolicyTagRequest;
import org.project.soar.model.youthpolicytag.dto.YouthPolicyTagResponse;
import org.project.soar.model.youthpolicytag.service.YouthPolicyTagService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/youth-policy-tag")
public class YouthPolicyTagController {
    public final YouthPolicyTagService youthPolicyTagService;
    public final TokenProvider tokenProvider;
    private final UserTagService userTagService;
    @GetMapping("/")
    @Operation(summary="전체 청년 정책 태그 조회", description="모든 청년 정책 태그를 조회합니다.")
    public ResponseEntity<ApiResponse<List<YouthPolicyTagResponse>>> getAllYouthPolicyTag() {
        List<YouthPolicyTagResponse> result = youthPolicyTagService.getAllYouthPolicyTag();
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @GetMapping("/tag")
    @Operation(summary = "하나의 태그로 정책 조회")
    public ResponseEntity<ApiResponse<FindYouthPolicyByTagResponse>> getYouthPolicyTagByTagId(@RequestParam("tagId") Long tagId) {
        FindYouthPolicyByTagResponse result = youthPolicyTagService.getYouthPolicyTagByTagId(tagId);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @GetMapping("/tags")
    @Operation(summary = "여러 개의 태그로 정책 조회")
    public ResponseEntity<ApiResponse<YouthPolicyTagsResponse>> getYouthPolicyTagByTagIds(@RequestParam("tagIds") List<Long> tagIds) {
        YouthPolicyTagsResponse result = youthPolicyTagService.getAllYouthPolicyByTagIds(tagIds);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @GetMapping("/user")
    @Operation(summary = "사용자의 태그로 정책 매칭")
    public ResponseEntity<ApiResponse<?>> getYouthPolicyByUser(HttpServletRequest request) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        YouthPolicyTagsResponse result = youthPolicyTagService.getYouthPolicyByUser(user);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @PostMapping("/")
    @Operation(summary = "수동 청년 정책 태그 생성")
    public ResponseEntity<ApiResponse<List<YouthPolicyTagResponse>>> createYouthPolicyTag(@RequestBody YouthPolicyTagRequest youthPolicyTagRequest) {
        List<YouthPolicyTagResponse> result = youthPolicyTagService.createYouthPolicyTag(youthPolicyTagRequest);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @GetMapping("/tags/qs")
    @Operation(
            summary = "여러 태그로 정책 검색(우선순위 정렬)",
            description = """
        tagIds로 정책을 검색합니다.
        - 정책이 매칭된 태그 개수만큼 점수(+1) 부여
        - 점수 내림차순, 동점 시 createdAt 내림차순
        - 결과는 YouthPolicyMainItemDto의 Page로 반환 (searchMulti와 동일)
        """
    )
    public ResponseEntity<ApiResponse<?>> getYouthPolicyTagByTagIds(
            @RequestParam("tags") String tags,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpServletRequest request
    ) {

        User user = tokenProvider.getUserFromRequest(request);

        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        // "1,2,3" 또는 줄바꿈 혼합 입력 지원 → Long 리스트로 변환
        final List<Long> tagIds;
        try {
            tagIds = Arrays.stream(tags.split("[,\\n]"))   // 콤마/줄바꿈 둘 다 허용
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(Long::valueOf)                     // 숫자 변환
                    .distinct()
                    .toList();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createError("태그 ID는 숫자여야 합니다. 입력값: " + tags));
        }

        // ★ 추가: 사용자 태그 저장 (기존 것 삭제 후 새로 세팅)
        try {
            userTagService.setUserTag(user, new org.project.soar.model.usertag.dto.UserTagRequest(tagIds));
        } catch (IllegalArgumentException badTag) {
            // 존재하지 않는 태그가 포함된 경우
            return ResponseEntity.badRequest().body(ApiResponse.createError(badTag.getMessage()));
        } catch (Exception saveEx) {
            // 저장 실패해도 검색은 계속 진행하고 싶다면 로그만 남기고 계속 가기
            // 저장도 실패 시 요청 자체를 막고 싶다면 500 반환으로 바꾸세요.
            log.error("사용자 태그 저장 실패 userId={}, tagIds={}", user.getUserId(), tagIds, saveEx);
        }


        Pageable pageable = PageRequest.of(page, size);
        var result = youthPolicyTagService.multiTagSearchPrioritizedMain(user, tagIds, pageable);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }
}
