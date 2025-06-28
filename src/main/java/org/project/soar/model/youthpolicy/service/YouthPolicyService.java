package org.project.soar.model.youthpolicy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.global.config.YouthPolicyApiConfig;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.dto.*;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouthPolicyService {

    private final YouthPolicyRepository youthPolicyRepository;
    private final YouthPolicyApiConfig youthPolicyApiConfig;
    private final RestTemplate restTemplate;

    /**
     * 전체 청년정책 데이터 동기화 (Controller와 Scheduler에서 사용)
     */
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
                            List<YouthPolicy> youthPolicyList = convertToYouthPolicyEntityList(youthPolicyApiDataList);
                            int savedCount = saveYouthPolicyList(youthPolicyList);
                            totalSavedCount += savedCount;

                            log.info("Processed {} policies from page {}, saved: {}",
                                    youthPolicyApiDataList.size(), pageNum, savedCount);

                            // 다음 페이지 존재 여부 확인
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

            // 최근 등록된 정책 수 카운트
            long recentCount = youthPolicyRepository.countByCreateDateAfter(cutoffDate);
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
    public List<YouthPolicy> searchByKeyword(String keyword) {
        try {
            if (!StringUtils.hasText(keyword)) {
                return getAllYouthPolicies();
            }

            // 제목, 키워드, 설명에서 검색
            return youthPolicyRepository.findByTitleContainingOrKeywordsContainingOrDescriptionContaining(
                    keyword, keyword, keyword);
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

            // 대분류 또는 중분류로 검색
            return youthPolicyRepository.findByLargeClassificationContainingOrMediumClassificationContaining(
                    category, category);
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
     * 현재 신청 가능한 청년정책 목록 조회 (Controller에서 사용)
     */
    public List<YouthPolicy> getCurrentAvailablePolicies() {
        try {
            LocalDateTime now = LocalDateTime.now();
            return youthPolicyRepository.findCurrentlyApplicablePolicies(now);
        } catch (Exception e) {
            log.error("Error getting current available policies", e);
            throw new RuntimeException("현재 신청 가능한 청년정책 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 청년정책 통계 조회 (Controller에서 사용)
     */
    public Map<String, Object> getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 전체 정책 수
            long totalCount = youthPolicyRepository.count();
            stats.put("totalCount", totalCount);

            // 카테고리별 통계
            Map<String, Long> categoryStats = new HashMap<>();
            List<Object[]> categoryResults = youthPolicyRepository.countByLargeClassification();
            for (Object[] result : categoryResults) {
                String category = (String) result[0];
                Long count = (Long) result[1];
                if (category != null && !category.trim().isEmpty()) {
                    categoryStats.put(category, count);
                }
            }
            stats.put("byCategory", categoryStats);

            // 현재 신청 가능한 정책 수
            LocalDateTime now = LocalDateTime.now();
            long currentAvailableCount = youthPolicyRepository.countCurrentlyApplicablePolicies(now);
            stats.put("currentAvailable", currentAvailableCount);

            // 월별 등록 통계 (최근 12개월)
            Map<String, Long> monthlyStats = new HashMap<>();
            LocalDateTime twelveMonthsAgo = now.minusMonths(12);
            List<Object[]> monthlyResults = youthPolicyRepository.countByCreateDateGroupByMonth(twelveMonthsAgo);
            for (Object[] result : monthlyResults) {
                String month = result[0].toString();
                Long count = (Long) result[1];
                monthlyStats.put(month, count);
            }
            stats.put("monthlyRegistrations", monthlyStats);

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

    /**
     * 단일 API DTO를 Entity로 변환
     */
    private YouthPolicy convertToYouthPolicyEntity(YouthPolicyApiData youthPolicyApiData) {
        YouthPolicy youthPolicyEntity = YouthPolicy.builder()
                // 기본 정보 - 모든 필드에 검증 적용
                .policyId(truncateString(youthPolicyApiData.getPlcyNo(), 100))
                .title(truncateString(youthPolicyApiData.getPlcyNm(), 2000))
                .projectName(truncateString(youthPolicyApiData.getPlcyNm(), 2000))
                .description(youthPolicyApiData.getPlcyExplnCn()) // TEXT 타입이므로 검증 불필요
                .keywords(truncateString(youthPolicyApiData.getPlcyKywdNm(), 2000))
                .supportContent(youthPolicyApiData.getPlcySprtCn()) // TEXT 타입이므로 검증 불필요
                .applyUrl(truncateString(youthPolicyApiData.getAplyUrlAddr(), 3000))
                .applyPeriod(truncateString(youthPolicyApiData.getAplyYmd(), 1000))

                // 기관 정보
                .supervisingInstitution(truncateString(youthPolicyApiData.getSprvsnInstCdNm(), 1000))
                .operatingInstitution(truncateString(youthPolicyApiData.getOperInstCdNm(), 1000))

                // 분류 정보
                .largeClassification(truncateString(youthPolicyApiData.getLclsfNm(), 500))
                .mediumClassification(truncateString(youthPolicyApiData.getMclsfNm(), 500))

                // 🔥 핵심: business_period_etc 검증 추가
                .businessPeriodEtc(truncateString(youthPolicyApiData.getBizPrdEtcCn(), 2000))

                // 기타 정보
                .supportScale(truncateString(youthPolicyApiData.getSprtSclCnt(), 1000))
                .referenceUrl1(truncateString(youthPolicyApiData.getRefUrlAddr1(), 3000))
                .referenceUrl2(truncateString(youthPolicyApiData.getRefUrlAddr2(), 3000))
                .applyMethodContent(youthPolicyApiData.getPlcyAplyMthdCn()) // TEXT 타입
                .screeningMethodContent(youthPolicyApiData.getSrngMthdCn()) // TEXT 타입
                .submitDocumentContent(youthPolicyApiData.getSbmsnDcmntCn()) // TEXT 타입
                .etcMatterContent(youthPolicyApiData.getEtcMttrCn()) // TEXT 타입
                .build();

        // 신청 기간 파싱
        parseAndSetApplicationPeriod(youthPolicyApiData.getAplyYmd(), youthPolicyEntity);

        // 나이 제한 파싱
        parseAndSetAgeLimit(youthPolicyApiData, youthPolicyEntity);

        // 조회수 파싱
        parseAndSetInquiryCount(youthPolicyApiData.getInqCnt(), youthPolicyEntity);

        return youthPolicyEntity;
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
        log.warn("String truncated: [{}] from {} to {} characters. Original: '{}'",
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                trimmed.length(),
                maxLength,
                trimmed.substring(0, Math.min(50, trimmed.length())) + "...");

        return trimmed.substring(0, maxLength);
    }

    /**
     * 신청 기간 문자열 파싱 및 설정
     */
    private void parseAndSetApplicationPeriod(String applyPeriodString, YouthPolicy youthPolicyEntity) {
        if (!StringUtils.hasText(applyPeriodString)) {
            return;
        }

        try {
            String[] dateStringArray = applyPeriodString.split("\\s*~\\s*");
            if (dateStringArray.length == 2) {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                LocalDateTime applicationStartDate = LocalDate.parse(dateStringArray[0].trim(), dateFormatter)
                        .atStartOfDay();
                LocalDateTime applicationEndDate = LocalDate.parse(dateStringArray[1].trim(), dateFormatter).atTime(23,
                        59, 59);

                youthPolicyEntity.setApplicationStartDate(applicationStartDate);
                youthPolicyEntity.setApplicationEndDate(applicationEndDate);
            }
        } catch (Exception exception) {
            log.warn("Failed to parse apply period: {} - {}", applyPeriodString, exception.getMessage());
        }
    }

    /**
     * 나이 제한 파싱 및 설정
     */
    private void parseAndSetAgeLimit(YouthPolicyApiData apiData, YouthPolicy entity) {
        try {
            if (StringUtils.hasText(apiData.getSprtTrgtMinAge()) && !apiData.getSprtTrgtMinAge().equals("0")) {
                entity.setMinAge(Integer.parseInt(apiData.getSprtTrgtMinAge()));
            }
            if (StringUtils.hasText(apiData.getSprtTrgtMaxAge()) && !apiData.getSprtTrgtMaxAge().equals("0")) {
                entity.setMaxAge(Integer.parseInt(apiData.getSprtTrgtMaxAge()));
            }
        } catch (NumberFormatException exception) {
            log.warn("Failed to parse age limit: min={}, max={}",
                    apiData.getSprtTrgtMinAge(), apiData.getSprtTrgtMaxAge());
        }
    }

    /**
     * 조회수 파싱 및 설정
     */
    private void parseAndSetInquiryCount(String inquiryCountString, YouthPolicy entity) {
        try {
            if (StringUtils.hasText(inquiryCountString)) {
                entity.setInquiryCount(Integer.parseInt(inquiryCountString));
            }
        } catch (NumberFormatException exception) {
            log.warn("Failed to parse inquiry count: {}", inquiryCountString);
            entity.setInquiryCount(0);
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
                if (existingYouthPolicy.isPresent()) {
                    YouthPolicy existingEntity = existingYouthPolicy.get();
                    updateExistingYouthPolicy(existingEntity, youthPolicyEntity);
                    youthPolicyRepository.save(existingEntity);
                } else {
                    youthPolicyRepository.save(youthPolicyEntity);
                    savedCount++;
                }
            } catch (Exception exception) {
                log.error("Failed to save youth policy: {} - {}",
                        youthPolicyEntity.getPolicyId(), exception.getMessage());
            }
        }

        return savedCount;
    }
    

    /**
     * 기존 청년정책 데이터 업데이트
     */
    private void updateExistingYouthPolicy(YouthPolicy existingYouthPolicy, YouthPolicy newYouthPolicyData) {
        existingYouthPolicy.setTitle(newYouthPolicyData.getTitle());
        existingYouthPolicy.setDescription(newYouthPolicyData.getDescription());
        existingYouthPolicy.setKeywords(newYouthPolicyData.getKeywords());
        existingYouthPolicy.setSupportContent(newYouthPolicyData.getSupportContent());
        existingYouthPolicy.setApplyUrl(newYouthPolicyData.getApplyUrl());
        existingYouthPolicy.setApplyPeriod(newYouthPolicyData.getApplyPeriod());
        existingYouthPolicy.setApplicationStartDate(newYouthPolicyData.getApplicationStartDate());
        existingYouthPolicy.setApplicationEndDate(newYouthPolicyData.getApplicationEndDate());
        existingYouthPolicy.setSupervisingInstitution(newYouthPolicyData.getSupervisingInstitution());
        existingYouthPolicy.setOperatingInstitution(newYouthPolicyData.getOperatingInstitution());
        existingYouthPolicy.setMinAge(newYouthPolicyData.getMinAge());
        existingYouthPolicy.setMaxAge(newYouthPolicyData.getMaxAge());
        existingYouthPolicy.setSupportScale(newYouthPolicyData.getSupportScale());
        existingYouthPolicy.setLargeClassification(newYouthPolicyData.getLargeClassification());
        existingYouthPolicy.setMediumClassification(newYouthPolicyData.getMediumClassification());
        existingYouthPolicy.setBusinessPeriodEtc(newYouthPolicyData.getBusinessPeriodEtc());
        existingYouthPolicy.setInquiryCount(newYouthPolicyData.getInquiryCount());
        existingYouthPolicy.setReferenceUrl1(newYouthPolicyData.getReferenceUrl1());
        existingYouthPolicy.setReferenceUrl2(newYouthPolicyData.getReferenceUrl2());
        existingYouthPolicy.setApplyMethodContent(newYouthPolicyData.getApplyMethodContent());
        existingYouthPolicy.setScreeningMethodContent(newYouthPolicyData.getScreeningMethodContent());
        existingYouthPolicy.setSubmitDocumentContent(newYouthPolicyData.getSubmitDocumentContent());
        existingYouthPolicy.setEtcMatterContent(newYouthPolicyData.getEtcMatterContent());
    }
}