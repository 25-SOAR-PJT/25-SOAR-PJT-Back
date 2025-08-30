package org.project.soar.model.youthpolicy.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.soar.config.TokenProvider;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.global.scheduler.YouthPolicyScheduler;
import org.project.soar.model.user.User;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicyStep;
import org.project.soar.model.youthpolicy.dto.CalendarDayResponseDto;
import org.project.soar.model.youthpolicy.dto.YouthPolicyEndDateItemDto;
import org.project.soar.model.youthpolicy.dto.YouthPolicyMainItemDto;
import org.project.soar.model.youthpolicy.repository.YouthPolicyStepRepository;
import org.project.soar.model.youthpolicy.service.YouthPolicyService;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
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
    public final TokenProvider tokenProvider;

    @PostMapping("/sync")
    @Operation(summary = "청년정책 데이터 동기화", description = "외부 API에서 청년정책 데이터를 수집/갱신합니다. 완료 후 현재 저장된 전체 개수를 반환합니다.")
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
    @Operation(summary = "정책 단계(신청/서류/발표/주의) 조회", description = "정책 ID로 추출/저장된 최신 단계 정보를 조회합니다. 데이터가 없다면 안내 메시지를 반환합니다.")
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
    @Operation(summary = "정책 목록 조회(키워드/카테고리 필터)", description = "키워드 또는 카테고리로 정책을 필터링하여 조회합니다. 파라미터가 없으면 전체 목록을 반환합니다.")
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
    @Operation(summary = "정책 검색(정책명 전용 또는 전체 컬럼)", description = """
            keyword로 정책을 검색합니다.
            - nameOnly=false(기본): 정책명/키워드/설명/지원내용/신청방법 등 **전체 컬럼**을 대상으로 검색
            - nameOnly=true: **정책명만** 대상으로 검색
            페이징/정렬 파라미터(page, size, sortBy, sortDir) 지원.
            """)
    public ResponseEntity<ApiResponse<?>> search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "nameOnly", defaultValue = "false") boolean nameOnly,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            var result = nameOnly
                    ? youthPolicyService.searchByPolicyNamePagedDto(keyword, pageable)
                    : youthPolicyService.searchEverywherePagedDto(keyword, pageable);

            return ResponseEntity.ok(ApiResponse.createSuccess(result));
        } catch (Exception e) {
            log.error("정책 키워드 검색 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.createError("검색 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/paged")
    @Operation(summary = "정책 목록 페이징 조회", description = "정렬/페이지/사이즈를 지정하여 정책 목록을 페이징으로 조회합니다.")
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
    @Operation(summary = "정책 상세 조회", description = "정책 ID로 단일 정책 상세 정보를 조회합니다.")
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
    @Operation(summary = "현재 신청 가능 정책 조회(간단판)", description = "현재 신청 가능하다고 간주되는 정책 목록을 반환합니다. (추후 실제 날짜/상태 기반 필터링으로 고도화 예정)")
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
    @Operation(summary = "정책 통계 조회", description = "정책 수, 카테고리 별 개수, 월별 등록 현황 등 간단 통계를 반환합니다.")
    public ResponseEntity<ApiResponse<?>> getYouthPolicyStats() {
        try {
            return ResponseEntity.ok(ApiResponse.createSuccess(youthPolicyService.getStatistics()));
        } catch (Exception e) {
            log.error("정책 통계 조회 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.createError("정책 통계 조회 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "헬스 체크", description = "DB 연결 상태 및 전체 정책 수를 반환합니다.")
    public ResponseEntity<ApiResponse<?>> healthCheck() {
        try {
            long totalCount = youthPolicyService.getTotalCount();
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(totalCount, "DB 연결 정상"));
        } catch (Exception e) {
            log.error("헬스 체크 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.createError("헬스 체크 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/latest")
    @Operation(summary = "마감 임박 정책 조회", description = "사업 종료일이 가까운 순으로 정책을 페이징 조회합니다.")
    public ResponseEntity<ApiResponse<?>> getLatestPoliciesByEndDate() {
        try {
            Page<YouthPolicy> policies = youthPolicyService.getLatestPoliciesByEndDate();

            return ResponseEntity.ok(ApiResponse.createSuccess(policies));
        } catch (Exception e) {
            log.error("마감임박 지원사업 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.createError("마감임박 지원사업 조회 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/calendar/month")
    @Operation(summary = "[캘린더] 월별 일자별 집계", description = "특정 연/월에 대해, 날짜별로 '신청 마감 수'와 '사업 마감 수'를 집계해 반환합니다. 값이 존재하는 날짜만 반환합니다.")
    public ResponseEntity<ApiResponse<?>> getCalendarMonthCounts(
            @RequestParam int year,
            @RequestParam int month) {
        try {
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest().body(ApiResponse.createError("month는 1~12 범위여야 합니다."));
            }
            List<CalendarDayResponseDto> result = youthPolicyService.getCalendarMonthCounts(year, month);
            return ResponseEntity.ok(ApiResponse.createSuccess(result));
        } catch (Exception e) {
            log.error("월별 캘린더 카운트 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.createError("월별 캘린더 카운트 조회 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/calendar/day")
    @Operation(summary = "[캘린더] 특정 일의 개수 + 정책 요약", description = "요청한 날짜(yyyy-MM-dd)에 대해 '신청 마감'과 '사업 마감' 개수 및 해당일 마감되는 정책 요약(정책ID, 정책명, 마감일, dateLabel)을 반환합니다.")
    public ResponseEntity<ApiResponse<?>> getCalendarDayCounts(@RequestParam String date) {
        try {
            LocalDate target = LocalDate.parse(date); // ISO yyyy-MM-dd
            CalendarDayResponseDto result = youthPolicyService.getPoliciesByDay(target);
            return ResponseEntity.ok(ApiResponse.createSuccess(result));
        } catch (Exception e) {
            log.error("일별 캘린더 카운트 조회 실패: {}", date, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.createError("일별 캘린더 카운트 조회 실패: " + e.getMessage()));
        }
    }


    @GetMapping("/main") // 기존 API와 경로가 겹치지 않도록 "/main"으로 지정
    @Operation(summary = "정책 목록 조회(키워드/카테고리 필터 Main용)", description = "Main용 키워드 또는 카테고리로 정책을 필터링하여 조회합니다. 파라미터가 없으면 전체 목록을 페이징하여 반환합니다.")
    public ResponseEntity<ApiResponse<?>> getMainYouthPolicies(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir
    ) {
        try {
            // 1. 정렬(Sort) 및 페이징(Pageable) 객체 생성
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            // 2. 새로 추가한 서비스 메서드를 호출하여 페이징된 결과를 받음
            Page<YouthPolicyMainItemDto> policiesPage = youthPolicyService.searchByKeywordAndCategoryPagedMain(keyword, category, pageable);

            // 3. 성공 응답 반환
            return ResponseEntity.ok(ApiResponse.createSuccess(policiesPage));

        } catch (Exception e) {
            log.error("메인 청년정책 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.createError("데이터 조회 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/search/multi")
    @Operation(
            summary = "멀티 키워드 검색(우선순위 정렬)",
            description = """
        콤마로 구분된 여러 키워드로 검색합니다.
        - 각 키워드마다 기존 '전체 컬럼 검색 알고리즘'으로 매칭된 정책을 수집합니다.
        - 정책이 매칭된 키워드 개수만큼 우선순위 점수(+1씩)를 부여합니다.
        - 우선순위 내림차순, 동점 시 createdAt 내림차순으로 정렬합니다.
        - 결과는 YouthPolicyMainItemDto 페이지로 반환합니다.
        예) keywords=장학금, 서울시 강남구, 대학재학, 기혼자
        """
    )
    public ResponseEntity<ApiResponse<?>> multiKeywordSearchPrioritized(
            @RequestParam("keywords") String keywords,           // "장학금, 서울시 강남구, ..."
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            HttpServletRequest request
    ) {
        try {
            // 콤마/줄바꿈 구분자로 스플릿 후 정리
            List<String> keywordList = Arrays.stream(keywords.split("[,\\n]"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .toList();


            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            User user = tokenProvider.getUserFromRequest(request);
            if (user == null) {
                var result = youthPolicyService.multiKeywordSearchPrioritizedMain(keywordList, pageable);
                return ResponseEntity.ok(ApiResponse.createSuccess(result));
            }

            var pageDto = youthPolicyService.searchByKeywordAndCategoryPagedMainWithBookmark(
                    user, keywords, null, pageable);
            return ResponseEntity.ok(ApiResponse.createSuccess(pageDto));

        } catch (Exception e) {
            log.error("멀티 키워드 검색 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.createError("멀티 키워드 검색 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/search/by-ids")
    @Operation(
            summary = "정책 ID 리스트로 지정 순서 반환(메인 카드 형태)",
            description = "요청한 policyIds 순서대로 YouthPolicyMainItemDto 리스트를 반환합니다. 로그인 시 bookmarked 여부가 반영됩니다."
    )
    public ResponseEntity<ApiResponse<?>> getPoliciesByIdsInOrder(
            @RequestBody org.project.soar.model.youthpolicy.dto.PolicyIdListRequestDto dto,
            HttpServletRequest request
    ) {
        try {
            if (dto == null || dto.getPolicyIds() == null || dto.getPolicyIds().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.createError("policyIds가 비어 있습니다."));
            }
            User user = tokenProvider.getUserFromRequest(request); // null이면 비로그인 처리
            var list = youthPolicyService.findMainItemsByIdsInOrder(dto.getPolicyIds(), user);
            String msg = String.format("요청 %d건 중 %d건 반환(입력 순서 유지)",
                    dto.getPolicyIds().size(), list.size());
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(list, msg));
        } catch (Exception e) {
            log.error("ID 순서 반환 API 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.createError("ID 순서 반환 실패: " + e.getMessage()));
        }
    }


    @GetMapping("/latestOne")
    @Operation(summary = "북마크 중 종료일이 가장 가까운 정책 1건")
    public ResponseEntity<ApiResponse<?>> getLatestPolicyByEndDate(HttpServletRequest request) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        YouthPolicyEndDateItemDto dto = (YouthPolicyEndDateItemDto) youthPolicyService.getLatestPolicyByEndDate(user);
        if (dto == null) {
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null, "북마크된 정책이 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.createSuccess(dto));
    }
}
