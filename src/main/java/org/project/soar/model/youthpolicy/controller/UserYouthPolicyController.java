package org.project.soar.model.youthpolicy.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.project.soar.config.TokenProvider;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.user.User;
import org.project.soar.model.user.repository.UserRepository;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.dto.*;
import org.project.soar.model.youthpolicy.service.UserYouthPolicyService;
import org.project.soar.model.youthpolicy.service.YouthPolicyBookmarkService;
import org.project.soar.model.youthpolicy.service.YouthPolicyService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-policies")
@Tag(name = "UserYouthPolicy", description = "사용자 정책 신청/북마크 관련 API")
@SecurityRequirement(name = "authorization")
public class UserYouthPolicyController {

    private final UserYouthPolicyService userYouthPolicyService;
    private final YouthPolicyBookmarkService bookmarkService;
    private final YouthPolicyService youthPolicyService;
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
    @GetMapping("/bookmarks/latest")
    public ResponseEntity<ApiResponse<YouthPolicyLatestResponseDto>> getLatestBookmarkByEndDate(HttpServletRequest request) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body((ApiResponse<YouthPolicyLatestResponseDto>) ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        YouthPolicyLatestResponseDto youthPolicy = bookmarkService.getLatestBookmarkByEndDate(user);
        if (youthPolicy == null) {
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null, "북마크된 정책이 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(youthPolicy, "가장 최근 종료일이 가까운 정책 조회됨"));
    }

    @Operation(summary = "인기 정책 조회", description = "전체 사용자 북마크 수를 기준으로 인기 정책을 조회합니다.")
    @GetMapping("/popular")
    @CrossOrigin(origins = "*")
    public ResponseEntity<ApiResponse<?>> getPopularPolicies() {
        try {
            List<YouthPolicy> popularPolicies = bookmarkService.getPopularPolicies();
            return ResponseEntity.ok(ApiResponse.createSuccess(popularPolicies));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.createError("인기 정책 조회 실패: " + e.getMessage()));
        }
    }

    @Operation(summary = "나이대별 인기 정책 조회", description = "사용자의 나이대 기준 인기 정책을 조회합니다.")
    @GetMapping("/popular/age-group")
    public ResponseEntity<ApiResponse<?>> getPopularPoliciesAge(HttpServletRequest request) {
        try {
            User user = tokenProvider.getUserFromRequest(request);
            if (user == null) {
                return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
            }

            List<YouthPolicy> popularPolicies = bookmarkService.getPopularPoliciesAge(user);
            return ResponseEntity.ok(ApiResponse.createSuccess(popularPolicies));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.createError("인기 정책 조회 실패: " + e.getMessage()));
        }
    }

    @Operation(summary = "신청한 정책 개수 조회", description = "사용자가 신청한 정책 개수를 조회합니다.")
    @GetMapping("/applied/count")
    public ResponseEntity<ApiResponse<?>> getAppliedPolicyCount(HttpServletRequest request) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        int count = userYouthPolicyService.getAppliedPolicyCount(user);
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(count, "신청한 정책 개수 조회됨"));
    }

    @PostMapping("/apply/bulk")
    @Operation(summary = "여러 정책 동시 신청(배치)", description = """
            로그인한 사용자가 전달한 policyIds 목록을 일괄 신청합니다.

            - 신청 URL은 반환하지 않습니다.
            - 각 정책별 처리 결과(APPLIED, ALREADY_APPLIED, APPLY_ENDED, BUSINESS_ENDED, OPEN_UPCOMING, NOT_FOUND)와
            전체 합계를 포함한 요약 카운트를 반환합니다.
            - applicationStartDate가 현재보다 미래인 경우 해당 정책은 OPEN_UPCOMING(오픈 예정)으로 처리합니다.
            - 존재하지 않는 정책은 NOT_FOUND로 처리하며, 예외를 던지지 않고 항목별 결과에 반영됩니다.
            """)
    public ResponseEntity<ApiResponse<?>> applyToPoliciesBulk(
            HttpServletRequest request,
            @RequestBody YouthPolicyBulkApplyRequestDto bulkRequest) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        if (bulkRequest == null || bulkRequest.getPolicyIds() == null || bulkRequest.getPolicyIds().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("policyIds가 비어 있습니다."));
        }

        YouthPolicyBulkApplyResponseDto resp = userYouthPolicyService.applyToPolicies(user, bulkRequest.getPolicyIds());

        String msg = String.format(
                "총 %d건 요청 중 신청 %d건, 이미 신청 %d건, 신청 마감 %d건, 사업 종료 %d건, 오픈 예정 %d건, 미존재 %d건",
                resp.getRequestedCount(),
                resp.getAppliedCount(),
                resp.getAlreadyAppliedCount(),
                resp.getApplyEndedCount(),
                resp.getBusinessEndedCount(),
                resp.getOpenUpcomingCount(),
                resp.getNotFoundCount());

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(resp, msg));
    }

    @GetMapping("/mainLogin")
    @Operation(summary = "메인용 정책 목록(로그인 + 북마크 여부)")
    public ResponseEntity<ApiResponse<?>> getMainYouthPoliciesWithBookmark( // ★ Boomark → Bookmark 로 수정
                                                                            @RequestParam(defaultValue = "") String keyword,
                                                                            @RequestParam(defaultValue = "") String category,
                                                                            @RequestParam(value = "page", defaultValue = "0") int page,
                                                                            @RequestParam(value = "size", defaultValue = "10") int size,
                                                                            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
                                                                            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
                                                                            HttpServletRequest request
    ) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        var pageDto = youthPolicyService.searchByKeywordAndCategoryPagedMainWithBookmark(
                user, keyword, category, pageable);

        return ResponseEntity.ok(ApiResponse.createSuccess(pageDto));
    }

    @GetMapping("/mainLoginTags")
    @Operation(summary = "메인용 정책 목록(로그인 + 북마크 여부 + 해당하는 태그 and 검색)")
    public ResponseEntity<ApiResponse<?>> getMainYouthPoliciesWithBookmarkByTags(
            @RequestParam(value = "tags", defaultValue = "") String tags,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            HttpServletRequest request
    ) {
        User user = tokenProvider.getUserFromRequest(request);

        // 태그 파싱 ("1,2,3" 또는 줄바꿈 혼합)
        final List<Long> tagIds;
        try {
            tagIds = Arrays.stream(tags.split("[,\\n]"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(Long::valueOf)
                    .distinct()
                    .toList();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createError("태그 ID는 숫자여야 합니다. 입력값: " + tags));
        }

        // 정렬/페이징 구성
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        var pageDto = youthPolicyService.searchByTagsAndCategoryPagedMainWithBookmark(
                user, tagIds, category, pageable);

        return ResponseEntity.ok(ApiResponse.createSuccess(pageDto));
    }

    @Operation(
            summary = "사용자 북마크(신청여부/태그 포함) 조회",
            description = "사용자가 북마크한 모든 정책을 신청완료 여부와 태그 리스트와 함께 조회합니다."
    )
    @GetMapping("/bookmarks/with-meta")
    public ResponseEntity<ApiResponse<?>> getBookmarksWithMeta(HttpServletRequest request) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        List<YouthPolicyBookmarkWithMetaResponseDto> list =
                bookmarkService.getUserBookmarksWithMeta(user);

        String message = String.format("북마크(확장) %d건 조회됨", list.size());
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(list, message));
    }

    @Operation(
            summary = "정책 북마크 일괄 해제",
            description = "전달된 policyId 리스트의 북마크를 한 번에 해제합니다. 존재하지 않거나 이미 해제된 항목은 건너뜁니다."
    )
    @PostMapping("/bookmarks/bulk-unbookmark")
    public ResponseEntity<ApiResponse<?>> unbookmarkPoliciesBulk(
            HttpServletRequest request,
            @RequestBody PolicyIdListRequestDto dto
    ) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        if (dto == null || dto.getPolicyIds() == null || dto.getPolicyIds().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("policyIds가 비어 있습니다."));
        }

        BulkUnbookmarkResponseDto result = bookmarkService.unbookmarkMany(user, dto.getPolicyIds());

        String msg = String.format(
                "요청 %d건 중 해제 %d건, 스킵 %d건",
                result.getRequestedCount(),
                result.getRemovedCount(),
                result.getSkippedCount()
        );
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(result, msg));
    }

    @Operation(
            summary = "정책 신청 토글",
            description = "신청되어 있으면 취소하고, 아니면 신청합니다. 신청 마감/종료 시 신청 시도는 에러로 반환합니다."
    )
    @PostMapping("/{policyId}/apply/toggle")
    public ResponseEntity<ApiResponse<?>> toggleApply(
            HttpServletRequest request,
            @Parameter(description = "정책 ID") @PathVariable String policyId) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        YouthPolicyApplyToggleResponseDto dto = userYouthPolicyService.toggleApply(user, policyId);

        // 에러 케이스: 서비스에서 message에 마감/종료/미존재 등을 넣어줌
        String msg = dto.getMessage() == null ? "" : dto.getMessage();
        boolean isError = msg.contains("종료") || msg.contains("마감") || msg.contains("정책을 찾을 수 없습니다.");
        if (isError) {
            return ResponseEntity.badRequest().body(ApiResponse.createError(msg));
        }

        String okMessage = dto.isApplied()
                ? String.format("정책 신청 완료: policyId=%s, userId=%d%s",
                dto.getPolicyId(), user.getUserId(),
                (dto.getApplyUrl() == null || dto.getApplyUrl().isBlank()) ? ", redirectUrl 없음" : ", redirectUrl 반환됨")
                : String.format("정책 신청 취소 완료: policyId=%s, userId=%d", dto.getPolicyId(), user.getUserId());

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(dto, okMessage));
    }

    @Operation(
            summary = "신청 완료 정책 목록(최신순)",
            description = "로그인한 사용자가 신청 완료한 모든 정책을 신청일 내림차순으로 반환합니다."
    )
    @GetMapping("/applied")
    public ResponseEntity<ApiResponse<?>> getAppliedPoliciesLatest(HttpServletRequest request) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        var list = userYouthPolicyService.getAppliedPoliciesLatest(user);
        String message = String.format("신청 완료 정책 %d건(최신순) 조회됨", list.size());
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(list, message));
    }

    @Operation(summary = "인기 정책 조회(이름 + id만)", description = "전체 사용자 북마크 수를 기준으로 인기 정책을 조회합니다.")
    @GetMapping("/popularName")
    @CrossOrigin(origins = "*")
    public ResponseEntity<ApiResponse<?>> getPopularPoliciesName() {
        try {
            List<YouthPolicyPopularView> popularPolicies = bookmarkService.getPopularPoliciesName();
            return ResponseEntity.ok(ApiResponse.createSuccess(popularPolicies));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.createError("인기 정책 조회 실패: " + e.getMessage()));
        }
    }


    @Operation(summary = "나이대별 인기 정책 조회(explore 형식 + 사용자 나이)", description = "사용자의 나이대 기준 인기 정책을 조회합니다.")
    @GetMapping("/popular/age-user")
    public ResponseEntity<ApiResponse<?>> getPopularPoliciesUserAge(HttpServletRequest request) {
        try {
            User user = tokenProvider.getUserFromRequest(request);
            if (user == null) {
                return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
            }
            if (user.getUserBirthDate() == null) {
                return ResponseEntity.badRequest().body(ApiResponse.createError("사용자의 생년월일이 없습니다. 마이페이지에서 생년월일을 등록해 주세요."));
            }


            List<YouthPolicyUserAgeItemDto> popularPolicies = bookmarkService.getPopularPoliciesUserAge(user);
            return ResponseEntity.ok(ApiResponse.createSuccess(popularPolicies));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.createError("인기 정책 조회 실패: " + e.getMessage()));
        }
    }


    @PostMapping("/apply/toggle/bulk")
    @Operation(summary = "여러 정책 신청 토글(배치)", description = """
            로그인한 사용자가 전달한 policyIds 목록을 일괄 토글합니다.

                - 신청되어 있지 않으면 '신규 신청' 처리, 이미 신청되어 있으면 '신청 취소' 처리합니다.
                - 단, 신청 시도 시 기존 신청 제약(오픈 예정/마감/종료 등)은 apply/bulk와 동일하게 적용됩니다.
                - 요청/응답 스키마는 /apply/bulk 와 동일합니다.
                  (alreadyAppliedCount는 '취소된 건수'로 해석됩니다)
                - 신청 URL은 반환하지 않습니다.
                - 각 정책별 처리 결과(APPLIED, ALREADY_APPLIED, APPLY_ENDED, BUSINESS_ENDED, OPEN_UPCOMING, NOT_FOUND)와
                  전체 합계를 포함한 요약 카운트를 반환합니다.
            """)
    public ResponseEntity<ApiResponse<?>> toggleApplyPoliciesBulk(
            HttpServletRequest request,
            @RequestBody YouthPolicyBulkApplyRequestDto bulkRequest
    ) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        if (bulkRequest == null || bulkRequest.getPolicyIds() == null || bulkRequest.getPolicyIds().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("policyIds가 비어 있습니다."));
        }

        YouthPolicyBulkApplyResponseDto resp =
                userYouthPolicyService.toggleApplyToPolicies(user, bulkRequest.getPolicyIds());

        // 메시지 포맷도 /apply/bulk 와 동일하게 유지합니다.
        String msg = String.format(
                "총 %d건 요청 중 신청 %d건, 이미 신청 %d건, 신청 마감 %d건, 사업 종료 %d건, 오픈 예정 %d건, 미존재 %d건",
                resp.getRequestedCount(),
                resp.getAppliedCount(),
                resp.getAlreadyAppliedCount(),   // 토글에서는 '취소된 건수' 의미
                resp.getApplyEndedCount(),
                resp.getBusinessEndedCount(),
                resp.getOpenUpcomingCount(),
                resp.getNotFoundCount()
        );

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(resp, msg));
    }

}
