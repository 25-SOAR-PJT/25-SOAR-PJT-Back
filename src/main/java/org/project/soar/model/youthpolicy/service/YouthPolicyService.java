package org.project.soar.model.youthpolicy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.soar.config.YouthPolicyApiConfig;
import org.project.soar.model.category.CategoryType;
import org.project.soar.model.category.repository.CategoryRepository;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicyStep;
import org.project.soar.model.youthpolicy.dto.*;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.project.soar.model.youthpolicy.repository.YouthPolicyStepRepository;
import org.project.soar.util.DateClassifier;
import org.project.soar.util.StepExtractor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");


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
                    }else {
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

    /**
     * 키워드로 청년정책 검색 (Controller에서 사용)
     */
    public List<YouthPolicy> searchPolicies(String keyword) {
        return youthPolicyRepository.searchByKeyword(keyword);
    }

    /**
     * 키워드로 청년정책 검색 (Controller에서 사용) - 이름
     */
    public List<YouthPolicy> searchByKeyword(String keyword) {
        try {
            if (!StringUtils.hasText(keyword)) {
                return getAllYouthPolicies();
            }
            return youthPolicyRepository.findByPolicyNameContaining(keyword);
        } catch (Exception e) {
            log.error("Error searching youth policies by keyword: {}", keyword, e);
            throw new RuntimeException("청년정책 키워드 검색 중 오류가 발생했습니다.", e);
        }
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
     * 
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
            return policies ;
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
    }