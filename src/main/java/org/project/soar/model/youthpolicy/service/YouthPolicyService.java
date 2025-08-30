package org.project.soar.model.youthpolicy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.soar.config.YouthPolicyApiConfig;
import org.project.soar.model.category.CategoryType;
import org.project.soar.model.category.repository.CategoryRepository;
import org.project.soar.model.user.User;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicyStep;
import org.project.soar.model.youthpolicy.dto.*;
import org.project.soar.model.youthpolicy.repository.YouthPolicyBookmarkRepository;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.project.soar.model.youthpolicy.repository.YouthPolicyStepRepository;
import org.project.soar.model.youthpolicytag.repository.PolicyTagMatchProjection;
import org.project.soar.model.youthpolicytag.repository.YouthPolicyTagRepository;
import org.project.soar.util.DateClassifier;
import org.project.soar.util.StepExtractor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouthPolicyService {

    private final YouthPolicyRepository youthPolicyRepository;
    private final YouthPolicyApiConfig youthPolicyApiConfig;
    private final RestTemplate restTemplate;
    private final YouthPolicyStepRepository stepRepository;
    private final CategoryRepository categoryRepository;
    private final YouthPolicyBookmarkRepository bookmarkRepository;
    private final YouthPolicyTagRepository youthPolicyTagRepository;


    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Pattern SEOUL_GU_PATTERN =
            Pattern.compile("서울\\s*시\\s*([가-힣]+)\\s*구");

    @Transactional
    public int syncAllYouthPolicies() {
        try {
            int pageNum = 1;
            int pageSize = 100;
            boolean hasMoreData = true;
            int totalSavedCount = 0;

            log.info("Starting youth policy data synchronization");

            while (hasMoreData) {
                String apiUrl = buildYouthPolicyApiUrl(pageNum, pageSize);
                log.debug("Fetching youth policies from page {}: {}", pageNum, apiUrl);

                try {
                    YouthPolicyApiResponse apiResponse = restTemplate.getForObject(apiUrl,
                            YouthPolicyApiResponse.class);

                    if (validateApiResponse(apiResponse)) {
                        List<YouthPolicyApiData> youthPolicyApiDataList = apiResponse.getResult().getYouthPolicyList();

                        if (youthPolicyApiDataList != null && !youthPolicyApiDataList.isEmpty()) {

                            int savedCount = saveYouthPolicyFromApi(youthPolicyApiDataList);
                            totalSavedCount += savedCount;

                            log.info("Processed {} policies from page {}, saved: {}",
                                    youthPolicyApiDataList.size(), pageNum, savedCount);

                            YouthPolicyApiPaging apiPaging = apiResponse.getResult().getPagging();
                            hasMoreData = checkHasMoreData(pageNum, pageSize, apiPaging.getTotCount());
                            pageNum++;
                        } else {
                            log.info("No more data found on page {}", pageNum);
                            hasMoreData = false;
                        }
                    } else {
                        log.error("Invalid API response on page {}: {}", pageNum,
                                apiResponse != null ? apiResponse.getResultMessage() : "No response");
                        hasMoreData = false;
                    }
                } catch (RestClientException restException) {
                    log.error("REST API call failed on page {}", pageNum, restException);
                    throw new RuntimeException("API 호출 중 네트워크 오류가 발생했습니다.", restException);
                }
            }

            log.info("Youth policy data synchronization completed. Total saved: {}", totalSavedCount);
            return totalSavedCount;

        } catch (Exception exception) {
            log.error("Error in youth policy data synchronization", exception);
            throw new RuntimeException("청년정책 데이터 동기화 중 오류가 발생했습니다.", exception);
        }
    }

    /**
     * 최근 N일간 등록된 청년정책만 동기화 (Scheduler에서 사용)
     */
    @Transactional
    public int syncRecentYouthPolicies(int days) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            log.info("Syncing recent youth policies registered after: {}", cutoffDate);

            // 전체 동기화를 수행하되, 최근 데이터만 우선 처리
            // 실제로는 API에서 날짜 필터링을 지원하지 않으므로 전체 동기화 후 필터링
            int totalSynced = syncAllYouthPolicies();

            // 최근 등록된 정책 수 카운트 - Repository 메서드 수정
            long recentCount = youthPolicyRepository.countByCreatedAtAfter(cutoffDate);
            log.info("Found {} policies registered in the last {} days", recentCount, days);

            return (int) recentCount;
        } catch (Exception e) {
            log.error("Error syncing recent youth policies", e);
            throw new RuntimeException("최근 청년정책 데이터 동기화 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 모든 청년정책 데이터 삭제 (Scheduler의 전체 재동기화에서 사용)
     */
    @Transactional
    public void deleteAllYouthPolicies() {
        try {
            long count = youthPolicyRepository.count();
            log.info("Deleting all {} youth policies for full resync", count);
            youthPolicyRepository.deleteAll();
            log.info("Successfully deleted all youth policy data");
        } catch (Exception e) {
            log.error("Error deleting all youth policies", e);
            throw new RuntimeException("청년정책 데이터 삭제 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 전체 청년정책 목록 조회 (Controller에서 사용)
     */
    public List<YouthPolicy> getAllYouthPolicies() {
        try {
            return youthPolicyRepository.findAll();
        } catch (Exception e) {
            log.error("Error getting all youth policies", e);
            throw new RuntimeException("청년정책 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    private String normalize(String q) {
        if (q == null) return "";
        // 한글/영문/숫자/공백을 제외한 모든 문자(온점/특수문자 포함)를 공백으로
        String cleaned = q.replaceAll("[^\\p{IsHangul}\\p{Alnum}\\s]+", " ");
        // 다중 공백 -> 단일 공백, 양끝 공백 제거
        return cleaned.trim().replaceAll("\\s+", " ");
    }

    private List<String> tokenize(String q) {
        String n = normalize(q);
        if (n.isEmpty()) return List.of();

        // 기본 토큰
        List<String> base = Arrays.stream(n.split(" "))
                .filter(t -> t.length() >= 1)
                .collect(Collectors.toCollection(ArrayList::new));

        // --- [특례] "서울시 XXX구" -> "서울", "XXX" ---
        // 입력 내 여러 번 등장해도 모두 처리
        Set<String> additions = new LinkedHashSet<>();
        Set<String> removals  = new HashSet<>();
        Matcher m = SEOUL_GU_PATTERN.matcher(n);
        while (m.find()) {
            String guRoot = m.group(1);  // 예: 강남, 서초, 동대문, 중 등
            additions.add("서울");
            additions.add(guRoot);
            removals.add("서울시");
            removals.add(guRoot + "구");
        }

        if (!removals.isEmpty()) {
            base.removeIf(removals::contains); // 원래 토큰에서 서울시/OO구 제거
            base.addAll(additions);            // "서울", "OO" 추가
        }

        // 소문자화 + 중복 제거 + 최대 10개 제한(기존 정책 유지)
        return base.stream()
                .map(String::toLowerCase)
                .distinct()
                .limit(10)
                .toList();
    }

    // 정책명 전용 검색 DTO 페이지
    public Page<YouthPolicySearchItemDto> searchByPolicyNamePagedDto(String keyword, Pageable pageable) {
        Page<YouthPolicy> page = searchByPolicyNamePaged(keyword, pageable);
        return new PageImpl<>(
                page.getContent().stream().map(YouthPolicySearchItemDto::from).toList(),
                pageable,
                page.getTotalElements());
    }

    // 전체 컬럼 검색 DTO 페이지
    public Page<YouthPolicySearchItemDto> searchEverywherePagedDto(String keyword, Pageable pageable) {
        Page<YouthPolicy> page = searchEverywherePaged(keyword, pageable);
        return new PageImpl<>(
                page.getContent().stream().map(YouthPolicySearchItemDto::from).toList(),
                pageable,
                page.getTotalElements());
    }

    /**
     * 정책명 전용 검색 (다중 키워드 AND, policyName LIKE %token%)
     * - 정확도 정렬: exact > startsWith > contains > createdAt desc
     */
    public Page<YouthPolicy> searchByPolicyNamePaged(String keyword, Pageable pageable) {
        List<String> tokens = tokenize(keyword);
        if (tokens.isEmpty()) return Page.empty(pageable);

        var spec = (org.springframework.data.jpa.domain.Specification<YouthPolicy>) (root, cq, cb) -> {
            var lowerName = cb.lower(root.get("policyName"));
            List<jakarta.persistence.criteria.Predicate> ands = new ArrayList<>();
            for (String t : tokens) {
                ands.add(cb.like(lowerName, "%" + t.toLowerCase() + "%"));
            }
            return cb.and(ands.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<YouthPolicy> page = youthPolicyRepository.findAll(spec, pageable);

        String qn = normalize(keyword).toLowerCase();
        List<YouthPolicy> sorted = page.getContent().stream()
                .sorted((a, b) -> {
                    String an = a.getPolicyName() == null ? "" : a.getPolicyName().toLowerCase();
                    String bn = b.getPolicyName() == null ? "" : b.getPolicyName().toLowerCase();

                    int aw = relevanceForName(an, qn);
                    int bw = relevanceForName(bn, qn);
                    if (aw != bw) return Integer.compare(bw, aw);

                    var ad = a.getCreatedAt();
                    var bd = b.getCreatedAt();
                    if (ad != null && bd != null) return bd.compareTo(ad);
                    if (ad != null) return -1;
                    if (bd != null) return 1;
                    return 0;
                })
                .toList();

        return new PageImpl<>(sorted, pageable, page.getTotalElements());
    }

    private int relevanceForName(String name, String q) {
        if (name.equals(q)) return 100;    // exact
        if (name.startsWith(q)) return 80; // startsWith
        if (name.contains(q)) return 60;   // contains
        int bonus = 0;
        for (String t : tokenize(q)) {
            if (name.contains(t)) bonus += 2;
        }
        return 40 + Math.min(10, bonus);
    }

    /**
     * 전체 컬럼 검색 (다중 키워드 AND, 각 토큰은 여러 컬럼 OR)
     * - 주의: policyId는 String이므로 숫자형 전환/비교 없음 (검색 단계에선 연도 필터 배제)
     */
    public Page<YouthPolicy> searchEverywherePaged(String keyword, Pageable pageable) {
        List<String> tokens = tokenize(keyword);
        if (tokens.isEmpty()) return Page.empty(pageable);

        var spec = (org.springframework.data.jpa.domain.Specification<YouthPolicy>) (root, cq, cb) -> {
            List<jakarta.persistence.criteria.Predicate> ands = new ArrayList<>();

            var cols = List.of(
                    root.get("policyName"),
                    root.get("policyKeyword"),
                    root.get("policyExplanation"),
                    root.get("policySupportContent"),
                    root.get("applyMethodContent"),
                    root.get("screeningMethodContent"),
                    root.get("submitDocumentContent"),
                    root.get("etcMatterContent"),
                    root.get("businessPeriodEtc"),
                    root.get("largeClassification"),
                    root.get("mediumClassification"),
                    root.get("supervisingInstName"),
                    root.get("operatingInstName"),
                    root.get("applyUrl"),
                    root.get("referenceUrl1"),
                    root.get("referenceUrl2"),
                    root.get("zipCode"),
                    root.get("policyMajorCode"),
                    root.get("jobCode"),
                    root.get("schoolCode"),
                    root.get("dateType"),
                    root.get("dateLabel")
            );

            for (String t : tokens) {
                String like = "%" + t.toLowerCase() + "%";
                List<jakarta.persistence.criteria.Predicate> ors = new ArrayList<>();
                for (var col : cols) {
                    ors.add(cb.like(cb.lower(col.as(String.class)), like));
                }
                ands.add(cb.or(ors.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            // (중요) policyId는 String → 숫자 비교/연도 파싱 시도하지 않음. 저장 단계에서 연도 필터링 완료 가정.
            return cb.and(ands.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<YouthPolicy> page = youthPolicyRepository.findAll(spec, pageable);

        String qn = normalize(keyword).toLowerCase();
        List<YouthPolicy> sorted = page.getContent().stream()
                .sorted((a, b) -> {
                    String an = a.getPolicyName() == null ? "" : a.getPolicyName().toLowerCase();
                    String bn = b.getPolicyName() == null ? "" : b.getPolicyName().toLowerCase();
                    int aw = relevanceForName(an, qn);
                    int bw = relevanceForName(bn, qn);
                    if (aw != bw) return Integer.compare(bw, aw);

                    Integer ai = a.getInquiryCount() == null ? 0 : a.getInquiryCount();
                    Integer bi = b.getInquiryCount() == null ? 0 : b.getInquiryCount();
                    if (!ai.equals(bi)) return Integer.compare(bi, ai);

                    var ad = a.getCreatedAt();
                    var bd = b.getCreatedAt();
                    if (ad != null && bd != null) return bd.compareTo(ad);
                    if (ad != null) return -1;
                    if (bd != null) return 1;
                    return 0;
                })
                .toList();

        return new PageImpl<>(sorted, pageable, page.getTotalElements());
    }

    /**
     * 기존 공개 메서드: 새 로직으로 위임 (호환 유지)
     */
    public List<YouthPolicy> searchPolicies(String keyword) {
        Page<YouthPolicy> p = searchEverywherePaged(keyword, PageRequest.of(0, 50));
        return p.getContent();
    }

    public List<YouthPolicy> searchByKeyword(String keyword) {
        if (!org.springframework.util.StringUtils.hasText(keyword)) {
            return getAllYouthPolicies();
        }
        Page<YouthPolicy> p = searchByPolicyNamePaged(keyword, PageRequest.of(0, 50));
        return p.getContent();
    }


    /**
     * 카테고리별 청년정책 조회 (Controller에서 사용)
     */
    public List<YouthPolicy> findByCategory(String category) {
        try {
            if (!StringUtils.hasText(category)) {
                return getAllYouthPolicies();
            }

            // Repository 메서드를 간단한 방식으로 수정
            return youthPolicyRepository.findByLargeClassificationContaining(category);
        } catch (Exception e) {
            log.error("Error finding youth policies by category: {}", category, e);
            throw new RuntimeException("청년정책 카테고리 검색 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 페이징된 청년정책 목록 조회 (Controller에서 사용)
     */
    public Page<YouthPolicy> getYouthPoliciesPaged(Pageable pageable) {
        try {
            return youthPolicyRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("Error getting paged youth policies", e);
            throw new RuntimeException("청년정책 페이징 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * ID로 특정 청년정책 조회 (Controller에서 사용)
     */
    public YouthPolicy getYouthPolicyById(String policyId) {
        try {
            return youthPolicyRepository.findById(policyId).orElse(null);
        } catch (Exception e) {
            log.error("Error getting youth policy by id: {}", policyId, e);
            throw new RuntimeException("청년정책 상세 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 현재 신청 가능한 청년정책 목록 조회 (Controller에서 사용) - 간단한 구현
     */
    public List<YouthPolicy> getCurrentAvailablePolicies() {
        try {
            // 간단하게 모든 정책 반환 (추후 필요시 날짜 필터링 추가)
            return youthPolicyRepository.findAll();
        } catch (Exception e) {
            log.error("Error getting current available policies", e);
            throw new RuntimeException("현재 신청 가능한 청년정책 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 청년정책 통계 조회 (Controller에서 사용) - 간단한 구현
     */
    public Map<String, Object> getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 전체 정책 수
            long totalCount = youthPolicyRepository.count();
            stats.put("totalCount", totalCount);

            // 간단한 통계만 제공
            stats.put("currentAvailable", totalCount);
            stats.put("byCategory", new HashMap<>());
            stats.put("monthlyRegistrations", new HashMap<>());

            return stats;
        } catch (Exception e) {
            log.error("Error getting youth policy statistics", e);
            throw new RuntimeException("청년정책 통계 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 전체 청년정책 수 조회 (Controller에서 사용)
     */
    public long getTotalCount() {
        try {
            return youthPolicyRepository.count();
        } catch (Exception e) {
            log.error("Error getting total count", e);
            throw new RuntimeException("청년정책 전체 수 조회 중 오류가 발생했습니다.", e);
        }
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * API URL 생성
     */
    private String buildYouthPolicyApiUrl(int pageNum, int pageSize) {
        return UriComponentsBuilder.fromHttpUrl(youthPolicyApiConfig.getBaseUrl())
                .queryParam("apiKeyNm", youthPolicyApiConfig.getKey())
                .queryParam("rtnType", "json")
                .queryParam("pageNum", pageNum)
                .queryParam("pageSize", pageSize)
                .build()
                .toString();
    }

    /**
     * API 응답 유효성 검사
     */
    private boolean validateApiResponse(YouthPolicyApiResponse apiResponse) {
        if (apiResponse == null) {
            log.warn("API response is null");
            return false;
        }
        if (apiResponse.getResultCode() != 200) {
            log.warn("API response code is not 200: {}", apiResponse.getResultCode());
            return false;
        }
        if (apiResponse.getResult() == null) {
            log.warn("API response result is null");
            return false;
        }
        return true;
    }

    /**
     * 다음 페이지 존재 여부 확인
     */
    private boolean checkHasMoreData(int currentPageNum, int pageSize, int totalCount) {
        return (currentPageNum * pageSize) < totalCount;
    }

    /**
     * API DTO List를 Entity List로 변환
     */
    private List<YouthPolicy> convertToYouthPolicyEntityList(List<YouthPolicyApiData> youthPolicyApiDataList) {
        return youthPolicyApiDataList.stream()
                .map(this::convertToYouthPolicyEntity)
                .collect(Collectors.toList());
    }

    private LocalDate parseDate(String str) {
        try {
            if (str != null && !str.trim().isEmpty()) {
                return LocalDate.parse(str.trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", str);
        }
        return null;
    }

    // 정책명에 과거 연도 포함 여부 검사
    private boolean containsPastYearInTitle(String title) {
        if (title == null)
            return false;

        Pattern yearPattern = Pattern.compile("20(\\d{2})");
        Matcher matcher = yearPattern.matcher(title);

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        while (matcher.find()) {
            int year = Integer.parseInt("20" + matcher.group(1));
            if (year < currentYear) {
                return true;
            }
        }

        return false;
    }

    /**
     * 단일 API DTO를 Entity로 변환 - 필드명 수정
     */
    private YouthPolicy convertToYouthPolicyEntity(YouthPolicyApiData data) {
        LocalDate applyStart = parseDate(data.getAplyBgngYmd());
        LocalDate applyEnd = parseDate(data.getAplyEndYmd());
        String bizEnd = data.getBizPrdEndYmd();

        if (containsPastYearInTitle(data.getPlcyNm())) {
            log.info("과거 연도 정책 제외됨: {}", data.getPlcyNm());
            return null;
        }

        DateClassifier.DateResult dateResult = DateClassifier.classify(
                applyStart,
                applyEnd,
                bizEnd,
                data.getPlcySprtCn(),
                data.getPlcyAplyMthdCn(),
                data.getSrngMthdCn(),
                data.getBizPrdEtcCn(),
                data.getPlcyNm(),
                LocalDate.now()
        );

        return YouthPolicy.builder()
                .policyId(truncateString(data.getPlcyNo(), 50))
                .policyName(truncateString(data.getPlcyNm(), 500))
                .policyKeyword(truncateString(data.getPlcyKywdNm(), 200))
                .policyExplanation(data.getPlcyExplnCn())
                .policySupportContent(data.getPlcySprtCn())

                // 분류
                .largeClassification(truncateString(data.getLclsfNm(), 200))
                .mediumClassification(truncateString(data.getMclsfNm(), 200))

                // 기관
                .supervisingInstCode(data.getSprvsnInstCd())
                .supervisingInstName(truncateString(data.getSprvsnInstCdNm(), 300))
                .operatingInstCode(data.getOperInstCd())
                .operatingInstName(truncateString(data.getOperInstCdNm(), 300))

                // 기간
                .businessPeriodStart(data.getBizPrdBgngYmd())
                .businessPeriodEnd(data.getBizPrdEndYmd())
                .businessPeriodEtc(truncateString(data.getBizPrdEtcCn(), 500))

                // 신청
                .applyMethodContent(data.getPlcyAplyMthdCn())
                .screeningMethodContent(data.getSrngMthdCn())
                .applyUrl(truncateString(data.getAplyUrlAddr(), 1000))
                .submitDocumentContent(data.getSbmsnDcmntCn())
                .etcMatterContent(data.getEtcMttrCn())

                // 기타
                .referenceUrl1(truncateString(data.getRefUrlAddr1(), 1000))
                .referenceUrl2(truncateString(data.getRefUrlAddr2(), 1000))

                // 대상
                .supportScaleCount(data.getSprtSclCnt())
                .supportTargetMinAge(parseInteger(data.getSprtTrgtMinAge()))
                .supportTargetMaxAge(parseInteger(data.getSprtTrgtMaxAge()))
                .supportTargetAgeLimitYn(data.getSprtTrgtAgeLmtYn())

                // 소득
                .earnMinAmt(parseLong(data.getEarnMinAmt()))
                .earnMaxAmt(parseLong(data.getEarnMaxAmt()))
                .earnEtcContent(truncateString(data.getEarnEtcCn(), 500))

                // 필터링용
                .additionalApplyQualification(data.getAddAplyQlfcCndCn())
                .inquiryCount(parseInteger(data.getInqCnt()))
                .zipCode(data.getZipCd())
                .policyMajorCode(truncateString(data.getPlcyMajorCd(), 100))
                .jobCode(truncateString(data.getJobCd(), 100))
                .schoolCode(truncateString(data.getSchoolCd(), 100))

                // 날짜
                .firstRegDt(parseDateTime(data.getFrstRegDt()))
                .lastModifyDt(parseDateTime(data.getLastMdfcnDt()))
                .applicationStartDate(applyStart != null ? applyStart.atStartOfDay() : null)
                .applicationEndDate(applyEnd != null ? applyEnd.atStartOfDay() : null)

                // 향상된 날짜 분류 반영
                .dateType(dateResult.type())
                .dateLabel(dateResult.label())

                .build();
    }

    /**
     * 개선된 문자열 길이 제한 유틸리티 메서드
     */
    private String truncateString(String str, int maxLength) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        String trimmed = str.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }

        // 길이가 초과하는 경우 로그 출력하고 절단
        log.warn("String truncated from {} to {} characters", trimmed.length(), maxLength);
        return trimmed.substring(0, maxLength);
    }

    /**
     * Integer 파싱 유틸리티
     */
    private Integer parseInteger(String str) {
        try {
            return str != null && !str.trim().isEmpty() ? Integer.parseInt(str.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Long 파싱 유틸리티
     */
    private Long parseLong(String str) {
        try {
            return str != null && !str.trim().isEmpty() ? Long.parseLong(str.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * DateTime 파싱 유틸리티
     */
    private LocalDateTime parseDateTime(String str) {
        try {
            if (str != null && !str.trim().isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse(str.trim(), formatter);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 데이터 전처리
     */

    private void preprocessAndSaveSteps(YouthPolicyApiData data) {
        Map<String, List<String>> steps = StepExtractor.extractSteps(
                data.getPlcyAplyMthdCn(),
                data.getSbmsnDcmntCn(),
                data.getSrngMthdCn());

        String policyId = data.getPlcyNo();
        String submittedDocs = data.getSbmsnDcmntCn() == null ? "없음" : data.getSbmsnDcmntCn();

        YouthPolicyStep newStep = YouthPolicyStep.builder()
                .policyId(policyId)
                .submittedDocuments(submittedDocs)
                .applyStep(StepExtractor.joinHtmlList(steps.get("apply")))
                .documentStep(StepExtractor.joinHtmlList(steps.get("document")))
                .noticeStep(StepExtractor.joinHtmlList(steps.get("notice")))
                .caution(StepExtractor.joinHtmlList(steps.get("caution")))
                .build();

        try {
            List<YouthPolicyStep> existing = stepRepository.findAllByPolicyId(policyId);
            if (!existing.isEmpty()) {
                log.info("기존 Step 삭제: {}", policyId);
                stepRepository.deleteAll(existing);
            }

            stepRepository.save(newStep);
            log.info("Step 저장 성공: {}", policyId);
        } catch (Exception e) {
            log.error("Step 저장 중 오류 발생: {}", policyId, e);
        }
    }

    private void saveCategoryForPolicy(YouthPolicy policy) {
        String lc = policy.getLargeClassification();
        if (lc == null || lc.trim().isEmpty()) return;

        Set<String> categoryNames = Arrays.stream(lc.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        for (String name : categoryNames) {
            CategoryType.fromName(name).ifPresent(categoryType -> {
                boolean exists = categoryRepository.findByCategoryCodeAndYouthPolicy(
                        categoryType.getCode(), policy).isPresent();
                if (!exists) {
                    categoryRepository.save(org.project.soar.model.category.Category.builder()
                            .categoryCode(categoryType.getCode())
                            .youthPolicy(policy)
                            .build());
                }
            });
        }
    }


    /**
     * 청년정책 데이터 저장 (중복 처리)
     */
    private int saveYouthPolicyList(List<YouthPolicy> youthPolicyList) {
        int savedCount = 0;

        for (YouthPolicy youthPolicyEntity : youthPolicyList) {
            try {
                Optional<YouthPolicy> existingYouthPolicy = youthPolicyRepository
                        .findById(youthPolicyEntity.getPolicyId());

                YouthPolicy savedPolicy;
                if (existingYouthPolicy.isPresent()) {
                    YouthPolicy existingEntity = existingYouthPolicy.get();
                    updateExistingYouthPolicy(existingEntity, youthPolicyEntity);
                    savedPolicy = youthPolicyRepository.save(existingEntity);
                } else {
                    savedPolicy = youthPolicyRepository.save(youthPolicyEntity);
                    savedCount++;
                    log.info("Saved new youth policy: {}", youthPolicyEntity.getPolicyId());
                }

                saveCategoryForPolicy(savedPolicy);

            } catch (Exception exception) {
                log.error("Failed to save youth policy: {} - {}", youthPolicyEntity.getPolicyId(),
                        exception.getMessage());
            }
        }

        return savedCount;
    }

    // public int saveYouthPolicyFromApi(List<YouthPolicyApiData> apiDataList) {
    //     // 1. 변환기: API 데이터를 YouthPolicy 엔티티로 변환
    //     List<YouthPolicy> entityList = apiDataList.stream()
    //             .map(this::convertToYouthPolicyEntity)
    //             .filter(Objects::nonNull)
    //             .collect(Collectors.toList());

    //     // 2. 정책 + 카테고리 저장: 기존 로직 재사용
    //     int savedCount = saveYouthPolicyList(entityList);

    //     // 3. 단계 저장: rawData 기준으로 전처리 + 저장
    //     for (YouthPolicyApiData rawData : apiDataList) {
    //         try {
    //             preprocessAndSaveSteps(rawData); // 기존 유지
    //         } catch (Exception e) {
    //             log.error("청년정책 단계 저장 중 오류 발생: {}", e.getMessage());
    //         }
    //     }

    //     return savedCount;
    // }

    /**
     * 청년정책 API 데이터 저장 (서울/경기 필터링 + 중복 처리)
     */
    public int saveYouthPolicyFromApi(List<YouthPolicyApiData> apiDataList) {
        int currentYear = LocalDate.now().getYear();

        // 1. 서울/경기 필터링 + 과거 연도 필터링
        List<YouthPolicyApiData> filteredList = apiDataList.stream()
                .filter(data -> {
                    String region = data.getRgtrUpInstCdNm();
                    return region != null && (region.contains("서울") || region.contains("경기"));
                })
                .filter(data -> {
                    String policyId = data.getPlcyNo();
                    if (policyId != null && policyId.length() >= 4) {
                        try {
                            int yearFromPolicyId = Integer.parseInt(policyId.substring(0, 4));
                            if (yearFromPolicyId < currentYear) {
                                log.info("과거 연도 정책 제외됨 (policyId={}): {}", policyId, data.getPlcyNm());
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            log.warn("정책 ID에서 연도 파싱 실패 (policyId={})", policyId);
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        log.info("원본 정책 개수: {}", apiDataList.size());
        log.info("서울/경기 + 연도 필터링 후 정책 개수: {}", filteredList.size());

        // 2. 정책 엔티티 변환
        List<YouthPolicy> entityList = filteredList.stream()
                .map(this::convertToYouthPolicyEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 3. 저장 (중복처리 + 카테고리 포함)
        int savedCount = saveYouthPolicyList(entityList);

        // 4. 단계 저장
        for (YouthPolicyApiData rawData : filteredList) {
            try {
                preprocessAndSaveSteps(rawData);
            } catch (Exception e) {
                log.error("정책 단계 저장 중 오류 발생 ({}): {}", rawData.getPlcyNo(), e.getMessage());
            }
        }

        return savedCount;
    }

    /**
     * 기존 청년정책 데이터 업데이트
     */
    private void updateExistingYouthPolicy(YouthPolicy existingYouthPolicy, YouthPolicy newYouthPolicyData) {
        existingYouthPolicy = existingYouthPolicy.builder()
                .policyName(newYouthPolicyData.getPolicyName())
                .policyKeyword(newYouthPolicyData.getPolicyKeyword())
                .policyExplanation(newYouthPolicyData.getPolicyExplanation())
                .policySupportContent(newYouthPolicyData.getPolicySupportContent())
                .largeClassification(newYouthPolicyData.getLargeClassification())
                .mediumClassification(newYouthPolicyData.getMediumClassification())
                .lastModifyDt(newYouthPolicyData.getLastModifyDt())
                .build();
    }

    /**
     * 마감임박 지원사업 리스트
     */
    public Page<YouthPolicy> getLatestPoliciesByEndDate() {
        try {
            Page<YouthPolicy> policies = youthPolicyRepository.findLatestPoliciesByEndDate(PageRequest.of(0, 10));
            return policies;
        } catch (Exception e) {
            log.error("Error getting latest policies by end date", e);
            throw new RuntimeException("마감임박 지원사업 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * [캘린더] 월별 일자별 개수(신청 마감/사업 마감)
     * - 응답: 해당 월에서 "값이 존재하는 날짜"만 List 형태로 반환
     */
    public List<CalendarDayResponseDto> getCalendarMonthCounts(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate first = ym.atDay(1);
        LocalDate last = ym.atEndOfMonth();

        LocalDateTime startDt = first.atStartOfDay();
        LocalDateTime endExclusive = last.plusDays(1).atStartOfDay();

        String startYmd = first.format(YMD);
        String endYmd = last.format(YMD);

        // 1) 신청 마감 집계
        Map<LocalDate, Integer> applyMap = new HashMap<>();
        for (Object[] row : youthPolicyRepository.countApplyEndByDayInRange(startDt, endExclusive)) {
            LocalDate d = ((java.sql.Date) row[0]).toLocalDate(); // FUNCTION('DATE', ...) 결과
            int cnt = ((Number) row[1]).intValue();
            applyMap.put(d, cnt);
        }

        // 2) 사업 마감 집계
        Map<LocalDate, Integer> bizMap = new HashMap<>();
        for (Object[] row : youthPolicyRepository.countBusinessEndByDayInRange(startYmd, endYmd)) {
            String ymd = (String) row[0];
            int cnt = ((Number) row[1]).intValue();
            LocalDate d = LocalDate.parse(ymd, YMD);
            bizMap.put(d, cnt);
        }

        // 3) 머지: 해당 월에서 값 있는 날짜만 생성
        Set<LocalDate> keys = new HashSet<>();
        keys.addAll(applyMap.keySet());
        keys.addAll(bizMap.keySet());

        List<CalendarDayResponseDto> result = new ArrayList<>();
        for (LocalDate d : keys) {
            result.add(CalendarDayResponseDto.builder()
                    .date(d)
                    .applyEndCount(applyMap.getOrDefault(d, 0))
                    .businessEndCount(bizMap.getOrDefault(d, 0))
                    .build());
        }

        // 날짜 오름차순 정렬
        result.sort(Comparator.comparing(CalendarDayResponseDto::getDate));
        return result;
    }

    /**
     * [캘린더] 특정 일의 개수 + 정책 요약(정책id, 정책명, 마감일)
     */
    public CalendarDayResponseDto getPoliciesByDay(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime endExclusive = date.plusDays(1).atStartOfDay();

        // 신청 마감(해당 일)
        List<YouthPolicy> applyEndPolicies = youthPolicyRepository.findByApplicationEndDateOn(start, endExclusive);

        // 사업 마감(해당 일)
        String ymd = date.format(YMD);
        List<YouthPolicy> businessEndPolicies = youthPolicyRepository.findByBusinessPeriodEnd(ymd);

        List<CalendarDayResponseDto.PolicySummary> summaries = new ArrayList<>();

        // 신청 마감 요약
        for (YouthPolicy p : applyEndPolicies) {
            LocalDate deadline = p.getApplicationEndDate() != null
                    ? p.getApplicationEndDate().toLocalDate()
                    : null;

            String label = (p.getDateLabel() != null && !p.getDateLabel().isBlank())
                    ? p.getDateLabel()
                    : "신청 마감";

            summaries.add(CalendarDayResponseDto.PolicySummary.builder()
                    .policyId(p.getPolicyId())
                    .policyName(p.getPolicyName())
                    .deadline(deadline) // LocalDate
                    .dateLabel(label)
                    .build());
        }

        // 사업 마감 요약
        for (YouthPolicy p : businessEndPolicies) {
            LocalDate deadline = null;
            String be = p.getBusinessPeriodEnd();
            if (StringUtils.hasText(be) && be.length() == 8) {
                try {
                    deadline = LocalDate.parse(be, YMD);
                } catch (Exception ignored) {
                }
            }
            String label = (p.getDateLabel() != null && !p.getDateLabel().isBlank())
                    ? p.getDateLabel()
                    : "사업 마감";

            summaries.add(CalendarDayResponseDto.PolicySummary.builder()
                    .policyId(p.getPolicyId())
                    .policyName(p.getPolicyName())
                    .deadline(deadline)
                    .dateLabel(label)
                    .build());
        }

        return CalendarDayResponseDto.builder()
                .date(date) // LocalDate
                .applyEndCount(applyEndPolicies.size())
                .businessEndCount(businessEndPolicies.size())
                .policies(summaries)
                .build();
    }

    public Page<YouthPolicyMainItemDto> searchByKeywordAndCategoryPagedMain(String keyword, String category, Pageable pageable) {
        Page<YouthPolicy> page = youthPolicyRepository.findByKeywordAndCategoryPaged(
                StringUtils.hasText(keyword) ? keyword : null,
                StringUtils.hasText(category) ? category : null,
                pageable);

        List<YouthPolicy> content = page.getContent();

        List<YouthPolicyMainItemDto> dtoList = content.stream().map(p ->
                YouthPolicyMainItemDto.builder()
                        .policyId(p.getPolicyId())
                        .policyName(p.getPolicyName())
                        .policyKeyword(p.getPolicyKeyword())
                        .largeClassification(p.getLargeClassification())
                        .mediumClassification(p.getMediumClassification())
                        .supervisingInstName(p.getSupervisingInstName())
                        .dateLabel(p.getDateLabel())
                        .bookmarked(false)
                        .build()
        ).toList();

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());

    }

    public Page<YouthPolicyMainItemDto> searchByKeywordAndCategoryPagedMainWithBookmark(
            User user, String keyword, String category, Pageable pageable) {

        Page<YouthPolicy> page = youthPolicyRepository.findByKeywordAndCategoryPaged(
                StringUtils.hasText(keyword) ? keyword : null,
                StringUtils.hasText(category) ? category : null,
                pageable);

        List<YouthPolicy> content = page.getContent();
        List<String> ids = content.stream().map(YouthPolicy::getPolicyId).toList();

        List<String> bookmarkedIds = bookmarkRepository.findBookmarkedPolicyIds(user, ids);
        var bookmarkedSet = new java.util.HashSet<>(bookmarkedIds);

        List<YouthPolicyMainItemDto> dtoList = content.stream().map(p ->
                YouthPolicyMainItemDto.builder()
                        .policyId(p.getPolicyId())
                        .policyName(p.getPolicyName())
                        .policyKeyword(p.getPolicyKeyword())
                        .largeClassification(p.getLargeClassification())
                        .mediumClassification(p.getMediumClassification())
                        .supervisingInstName(p.getSupervisingInstName())
                        .dateLabel(p.getDateLabel())
                        .bookmarked(bookmarkedSet.contains(p.getPolicyId()))
                        .build()
        ).toList();

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    public Page<YouthPolicyMainItemDto> multiKeywordSearchPrioritizedMain(List<String> keywords, Pageable pageable) {
        // 키워드가 없으면 빈 페이지
        if (keywords == null || keywords.isEmpty()) {
            return Page.empty(pageable);
        }

        // 1) "여러 키워드 중 하나라도 매칭"되는 전체 후보군 조회용 Specification (OR 결합)
        Specification<YouthPolicy> anyKeywordSpec = null;
        for (String kw : keywords) {
            Specification<YouthPolicy> perKw = createEverywhereSpecForSingleKeyword(kw);
            if (perKw == null) continue;
            anyKeywordSpec = (anyKeywordSpec == null) ? perKw : anyKeywordSpec.or(perKw);
        }
        if (anyKeywordSpec == null) {
            return Page.empty(pageable);
        }

        // 전체 후보군 조회 (정렬은 이후 스코어 계산 뒤 메모리에서 수행)
        List<YouthPolicy> candidates = youthPolicyRepository.findAll(anyKeywordSpec);

        // 2) 각 정책별 '매칭된 키워드 개수' 스코어링
        //    -> 동일한 "searchEverywherePaged" 알고리즘을 그대로 모사하여 in-memory 매칭 판정
        Map<String, Integer> scoreMap = new HashMap<>();
        for (YouthPolicy p : candidates) {
            int score = 0;
            for (String kw : keywords) {
                if (matchesEverywhereAlgorithm(p, kw)) {
                    score += 1;
                }
            }
            scoreMap.put(p.getPolicyId(), score);
        }

        // 3) 정렬: (score DESC) -> (createdAt DESC)
        List<YouthPolicy> sorted = new ArrayList<>(candidates);
        sorted.sort((a, b) -> {
            int sa = scoreMap.getOrDefault(a.getPolicyId(), 0);
            int sb = scoreMap.getOrDefault(b.getPolicyId(), 0);
            if (sa != sb) return Integer.compare(sb, sa); // score desc

            var ad = a.getCreatedAt();
            var bd = b.getCreatedAt();
            if (ad != null && bd != null) return bd.compareTo(ad); // 최신 우선
            if (ad != null) return -1;
            if (bd != null) return 1;
            return 0;
        });

        // 4) 페이지 잘라내기
        int total = sorted.size();
        int start = Math.min((int) pageable.getOffset(), total);
        int end = Math.min(start + pageable.getPageSize(), total);
        List<YouthPolicy> pageSlice = (start < end) ? sorted.subList(start, end) : List.of();

        // 5) DTO 매핑 (Main 아이템 DTO 포맷 유지)
        List<YouthPolicyMainItemDto> dtoList = pageSlice.stream().map(p ->
                YouthPolicyMainItemDto.builder()
                        .policyId(p.getPolicyId())
                        .policyName(p.getPolicyName())
                        .policyKeyword(p.getPolicyKeyword())
                        .largeClassification(p.getLargeClassification())
                        .mediumClassification(p.getMediumClassification())
                        .supervisingInstName(p.getSupervisingInstName())
                        .dateLabel(p.getDateLabel())
                        .bookmarked(false)
                        .build()
        ).toList();

        return new PageImpl<>(dtoList, pageable, total);
    }

    /**
     * 기존 searchEverywherePaged의 "단일 키워드"용 스펙 생성기
     * - 토큰 AND, 각 토큰은 여러 컬럼 OR
     */
    private Specification<YouthPolicy> createEverywhereSpecForSingleKeyword(String keyword) {
        List<String> tokens = tokenize(keyword);
        if (tokens.isEmpty()) return null;

        return (root, cq, cb) -> {
            List<Predicate> ands = new ArrayList<>();

            var cols = List.of(
                    root.get("policyName"),
                    root.get("policyKeyword"),
                    root.get("policyExplanation"),
                    root.get("policySupportContent"),
                    root.get("applyMethodContent"),
                    root.get("screeningMethodContent"),
                    root.get("submitDocumentContent"),
                    root.get("etcMatterContent"),
                    root.get("businessPeriodEtc"),
                    root.get("largeClassification"),
                    root.get("mediumClassification"),
                    root.get("supervisingInstName"),
                    root.get("operatingInstName"),
                    root.get("applyUrl"),
                    root.get("referenceUrl1"),
                    root.get("referenceUrl2"),
                    root.get("zipCode"),
                    root.get("policyMajorCode"),
                    root.get("jobCode"),
                    root.get("schoolCode"),
                    root.get("dateType"),
                    root.get("dateLabel")
            );

            for (String t : tokens) {
                String like = "%" + t.toLowerCase() + "%";
                List<Predicate> ors = new ArrayList<>();
                for (var col : cols) {
                    ors.add(cb.like(cb.lower(col.as(String.class)), like));
                }
                ands.add(cb.or(ors.toArray(new Predicate[0])));
            }

            return cb.and(ands.toArray(new Predicate[0]));
        };
    }

    /**
     * DB 스펙과 동일한 판정 로직을 메모리에서 수행
     * - 토큰 AND
     * - 각 토큰은 여러 컬럼 중 하나라도 포함되면 OK
     */
    private boolean matchesEverywhereAlgorithm(YouthPolicy p, String keyword) {
        List<String> tokens = tokenize(keyword);
        if (tokens.isEmpty()) return false;

        // Entity의 검색 대상 컬럼 문자열들
        List<String> cols = List.of(
                safeLower(p.getPolicyName()),
                safeLower(p.getPolicyKeyword()),
                safeLower(p.getPolicyExplanation()),
                safeLower(p.getPolicySupportContent()),
                safeLower(p.getApplyMethodContent()),
                safeLower(p.getScreeningMethodContent()),
                safeLower(p.getSubmitDocumentContent()),
                safeLower(p.getEtcMatterContent()),
                safeLower(p.getBusinessPeriodEtc()),
                safeLower(p.getLargeClassification()),
                safeLower(p.getMediumClassification()),
                safeLower(p.getSupervisingInstName()),
                safeLower(p.getOperatingInstName()),
                safeLower(p.getApplyUrl()),
                safeLower(p.getReferenceUrl1()),
                safeLower(p.getReferenceUrl2()),
                safeLower(p.getZipCode()),
                safeLower(p.getPolicyMajorCode()),
                safeLower(p.getJobCode()),
                safeLower(p.getSchoolCode()),
                safeLower(p.getDateType()),
                safeLower(p.getDateLabel())
        );

        // 모든 토큰이 "어느 한 컬럼"에는 포함되어야 함
        for (String t : tokens) {
            String tok = t.toLowerCase();
            boolean ok = false;
            for (String c : cols) {
                if (c != null && c.contains(tok)) {
                    ok = true; break;
                }
            }
            if (!ok) return false;
        }
        return true;
    }

    private String safeLower(String s) {
        return (s == null) ? "" : s.toLowerCase();
    }


    /**
     * 태그 AND + (옵션) 카테고리 필터 + 북마크 여부 포함 메인 목록
     * - tagIds의 모든 태그가 매칭된 정책만 반환
     * - category가 있으면 대/중분류에 포함되는 것만 필터
     * - pageable의 sort 기준을 최대한 반영(미지정/지원X 컬럼은 createdAt desc 기본)
     * - tagIds == null 이면 모든 정책(옵션: category) DB 페이징
     * - user == null 이면 bookmarked = false
     */
    public Page<YouthPolicyMainItemDto> searchByTagsAndCategoryPagedMainWithBookmark(
            User user, List<Long> tagIds, String category, Pageable pageable) {

        // 0) tagIds == null → 전체 정책(옵션: category) DB 페이징 경로
        if (tagIds == null || tagIds.isEmpty()) {
            Page<YouthPolicy> page = youthPolicyRepository.findByKeywordAndCategoryPaged(
                    null, // keyword 없음
                    org.springframework.util.StringUtils.hasText(category) ? category : null,
                    pageable
            );

            // 북마크 여부 (user == null 이면 전부 false)
            java.util.Set<String> bookmarkedSet;
            if (user != null && !page.isEmpty()) {
                var ids = page.getContent().stream().map(YouthPolicy::getPolicyId).toList();
                var bookmarkedIds = bookmarkRepository.findBookmarkedPolicyIds(user, ids);
                bookmarkedSet = new java.util.HashSet<>(bookmarkedIds);
            } else {
                bookmarkedSet = Collections.emptySet();
            }

            var dtoList = page.getContent().stream().map(p ->
                    YouthPolicyMainItemDto.builder()
                            .policyId(p.getPolicyId())
                            .policyName(p.getPolicyName())
                            .policyKeyword(p.getPolicyKeyword())
                            .largeClassification(p.getLargeClassification())
                            .mediumClassification(p.getMediumClassification())
                            .supervisingInstName(p.getSupervisingInstName())
                            .dateLabel(p.getDateLabel())
                            .bookmarked(user != null && bookmarkedSet.contains(p.getPolicyId()))
                            .build()
            ).toList();

            return new PageImpl<>(dtoList, pageable, page.getTotalElements());
        }

        // 기존 로직: tagIds가 빈 리스트면 빈 페이지
        if (tagIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 1) 정책별 매칭된 태그 개수 조회
        List<PolicyTagMatchProjection> matches = youthPolicyTagRepository.findPolicyMatchCounts(tagIds);
        if (matches.isEmpty()) {
            return Page.empty(pageable);
        }

        final int required = tagIds.size();

        // 2) 모든 태그가 매칭된 정책만 추리기 (AND)
        List<YouthPolicy> candidates = matches.stream()
                .filter(p -> {
                    Long cnt = p.getMatchCount();
                    return cnt != null && cnt.intValue() >= required;
                })
                .map(PolicyTagMatchProjection::getYouthPolicy)
                .toList();

        if (candidates.isEmpty()) {
            return Page.empty(pageable);
        }

        // 3) (옵션) 카테고리 필터
        List<YouthPolicy> filtered = candidates;
        if (org.springframework.util.StringUtils.hasText(category)) {
            String c = category.toLowerCase();
            filtered = candidates.stream()
                    .filter(p -> {
                        String lc = p.getLargeClassification() == null ? "" : p.getLargeClassification().toLowerCase();
                        String mc = p.getMediumClassification() == null ? "" : p.getMediumClassification().toLowerCase();
                        return lc.contains(c) || mc.contains(c);
                    })
                    .toList();
            if (filtered.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        // 4) 정렬
        java.util.Comparator<YouthPolicy> cmp = buildComparatorFromSort(pageable);
        List<YouthPolicy> sorted = filtered.stream().sorted(cmp).toList();

        // 5) 메모리 페이징
        int total = sorted.size();
        int start = Math.min((int) pageable.getOffset(), total);
        int end   = Math.min(start + pageable.getPageSize(), total);
        List<YouthPolicy> pageSlice = (start < end) ? sorted.subList(start, end) : java.util.List.of();

        // 6) 북마크 여부 (user == null 이면 전부 false)
        java.util.Set<String> bookmarkedSet;
        if (user != null && !pageSlice.isEmpty()) {
            var ids = pageSlice.stream().map(YouthPolicy::getPolicyId).toList();
            var bookmarkedIds = bookmarkRepository.findBookmarkedPolicyIds(user, ids);
            bookmarkedSet = new java.util.HashSet<>(bookmarkedIds);
        } else {
            bookmarkedSet = Collections.emptySet();
        }

        // 7) DTO 매핑
        List<YouthPolicyMainItemDto> dtoList = pageSlice.stream().map(p ->
                YouthPolicyMainItemDto.builder()
                        .policyId(p.getPolicyId())
                        .policyName(p.getPolicyName())
                        .policyKeyword(p.getPolicyKeyword())
                        .largeClassification(p.getLargeClassification())
                        .mediumClassification(p.getMediumClassification())
                        .supervisingInstName(p.getSupervisingInstName())
                        .dateLabel(p.getDateLabel())
                        .bookmarked(user != null && bookmarkedSet.contains(p.getPolicyId()))
                        .build()
        ).toList();

        return new PageImpl<>(dtoList, pageable, total);
    }

    /**
     * pageable.getSort()를 YouthPolicy 필드로 매핑하는 Comparator 생성
     * - 지원: createdAt, policyName, supervisingInstName, inquiryCount
     * - 미지정/미지원: createdAt DESC 기본
     */
    private java.util.Comparator<YouthPolicy> buildComparatorFromSort(Pageable pageable) {
        // 기본: createdAt desc
        java.util.Comparator<YouthPolicy> base =
                (a, b) -> {
                    var ad = a.getCreatedAt();
                    var bd = b.getCreatedAt();
                    if (ad != null && bd != null) return bd.compareTo(ad);
                    if (ad != null) return -1;
                    if (bd != null) return 1;
                    return 0;
                };

        if (pageable == null || pageable.getSort().isUnsorted()) {
            return base;
        }

        java.util.Comparator<YouthPolicy> cmp = null;

        for (org.springframework.data.domain.Sort.Order o : pageable.getSort()) {
            java.util.Comparator<YouthPolicy> next;

            switch (o.getProperty()) {
                case "createdAt" -> {
                    next = (a, b) -> {
                        var ad = a.getCreatedAt();
                        var bd = b.getCreatedAt();
                        int v = 0;
                        if (ad != null && bd != null) v = ad.compareTo(bd);
                        else if (ad != null) v = 1;
                        else if (bd != null) v = -1;
                        return o.isAscending() ? v : -v;
                    };
                }
                case "policyName" -> {
                    next = (a, b) -> {
                        String an = a.getPolicyName() == null ? "" : a.getPolicyName();
                        String bn = b.getPolicyName() == null ? "" : b.getPolicyName();
                        int v = an.compareToIgnoreCase(bn);
                        return o.isAscending() ? v : -v;
                    };
                }
                case "supervisingInstName" -> {
                    next = (a, b) -> {
                        String an = a.getSupervisingInstName() == null ? "" : a.getSupervisingInstName();
                        String bn = b.getSupervisingInstName() == null ? "" : b.getSupervisingInstName();
                        int v = an.compareToIgnoreCase(bn);
                        return o.isAscending() ? v : -v;
                    };
                }
                case "inquiryCount" -> {
                    next = (a, b) -> {
                        Integer ai = a.getInquiryCount() == null ? 0 : a.getInquiryCount();
                        Integer bi = b.getInquiryCount() == null ? 0 : b.getInquiryCount();
                        int v = ai.compareTo(bi);
                        return o.isAscending() ? v : -v;
                    };
                }
                default -> {
                    // 알 수 없는 정렬 컬럼은 createdAt로 대체
                    next = (a, b) -> {
                        var ad = a.getCreatedAt();
                        var bd = b.getCreatedAt();
                        int v = 0;
                        if (ad != null && bd != null) v = ad.compareTo(bd);
                        else if (ad != null) v = 1;
                        else if (bd != null) v = -1;
                        return o.isAscending() ? v : -v;
                    };
                }
            }

            cmp = (cmp == null) ? next : cmp.thenComparing(next);
        }

        return (cmp == null) ? base : cmp;
    }

    // YouthPolicyService.java
    public List<YouthPolicyMainItemDto> findMainItemsByIdsInOrder(List<String> policyIds, User user) {
        if (policyIds == null || policyIds.isEmpty()) return List.of();

        // null/공백 제거 + 입력 순서 보존하며 중복 제거
        List<String> ids = policyIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (ids.isEmpty()) return List.of();

        // 한 번에 로드 후 map으로 조회
        List<YouthPolicy> found = youthPolicyRepository.findAllById(ids);
        Map<String, YouthPolicy> map = found.stream()
                .collect(Collectors.toMap(YouthPolicy::getPolicyId, Function.identity()));

        // (옵션) 북마크 셋
        Set<String> bookmarked = Collections.emptySet();
        if (user != null) {
            List<String> bookmarkedIds = bookmarkRepository.findBookmarkedPolicyIds(user, ids);
            bookmarked = new HashSet<>(bookmarkedIds);
        }

        // 입력 순서대로 DTO 매핑(미존재 ID는 건너뜀)
        List<YouthPolicyMainItemDto> result = new ArrayList<>();
        for (String id : ids) {
            YouthPolicy p = map.get(id);
            if (p == null) continue;
            result.add(YouthPolicyMainItemDto.builder()
                    .policyId(p.getPolicyId())
                    .policyName(p.getPolicyName())
                    .policyKeyword(p.getPolicyKeyword())
                    .largeClassification(p.getLargeClassification())
                    .mediumClassification(p.getMediumClassification())
                    .supervisingInstName(p.getSupervisingInstName())
                    .dateLabel(p.getDateLabel())
                    .bookmarked(user != null && bookmarked.contains(p.getPolicyId()))
                    .build());
        }
        return result;
    }

    /**
     * 마감임박 지원사업 리스트
     */
    public YouthPolicyEndDateItemDto getLatestPolicyByEndDate(User user) {
        try {
            var policy = bookmarkRepository.findLatestBookmarkByEndDate(user, PageRequest.of(0, 1));
            return policy.isEmpty() ? null :  YouthPolicyEndDateItemDto.from(policy.get(0));
        } catch (Exception e) {
            log.error("Error getting latest policies by end date", e);
            throw new RuntimeException("마감임박 지원사업 조회 중 오류가 발생했습니다.", e);
        }
    }

}