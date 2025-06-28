// ========================================
// YouthPolicyRepository.java (수정된 버전)
// ========================================
// 파일 경로: src/main/java/org/project/soar/model/youthpolicy/repository/YouthPolicyRepository.java
package org.project.soar.model.youthpolicy.repository;

import org.project.soar.model.youthpolicy.YouthPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface YouthPolicyRepository extends JpaRepository<YouthPolicy, String> {

    // ========================================
    // Controller에서 사용하는 메서드들
    // ========================================

    /**
     * 제목, 키워드, 설명으로 검색 (키워드 검색용)
     */
    List<YouthPolicy> findByTitleContainingOrKeywordsContainingOrDescriptionContaining(
            String title, String keywords, String description);

    /**
     * 대분류 또는 중분류로 검색 (카테고리 검색용)
     */
    List<YouthPolicy> findByLargeClassificationContainingOrMediumClassificationContaining(
            String largeClassification, String mediumClassification);

    /**
     * 현재 신청 가능한 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE yp.applicationStartDate <= :now AND yp.applicationEndDate >= :now")
    List<YouthPolicy> findCurrentlyApplicablePolicies(@Param("now") LocalDateTime now);

    /**
     * 현재 신청 가능한 정책 수 카운트
     */
    @Query("SELECT COUNT(yp) FROM YouthPolicy yp WHERE yp.applicationStartDate <= :now AND yp.applicationEndDate >= :now")
    long countCurrentlyApplicablePolicies(@Param("now") LocalDateTime now);

    // ========================================
    // Scheduler에서 사용하는 메서드들
    // ========================================

    /**
     * 특정 날짜 이후 생성된 정책 수 카운트
     */
    long countByCreateDateAfter(LocalDateTime createDate);

    // ========================================
    // 통계용 메서드들
    // ========================================

    /**
     * 대분류별 정책 수 카운트
     */
    @Query("SELECT yp.largeClassification, COUNT(yp) FROM YouthPolicy yp WHERE yp.largeClassification IS NOT NULL GROUP BY yp.largeClassification")
    List<Object[]> countByLargeClassification();

    /**
     * 월별 등록 통계 (최근 N개월)
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', yp.createDate, '%Y-%m') as month, COUNT(yp) " +
            "FROM YouthPolicy yp " +
            "WHERE yp.createDate >= :fromDate " +
            "GROUP BY FUNCTION('DATE_FORMAT', yp.createDate, '%Y-%m') " +
            "ORDER BY month DESC")
    List<Object[]> countByCreateDateGroupByMonth(@Param("fromDate") LocalDateTime fromDate);

    /**
     * 신청 기간별 정책 조회
     */
    List<YouthPolicy> findByApplicationStartDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 특정 기관의 정책 조회
     */
    List<YouthPolicy> findBySupervisingInstitutionContaining(String institution);

    /**
     * 나이 제한이 있는 정책 조회
     */
    List<YouthPolicy> findByMinAgeIsNotNullOrMaxAgeIsNotNull();

    /**
     * 특정 나이 범위에 해당하는 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE " +
            "(yp.minAge IS NULL OR yp.minAge <= :age) AND " +
            "(yp.maxAge IS NULL OR yp.maxAge >= :age)")
    List<YouthPolicy> findPoliciesForAge(@Param("age") int age);

    /**
     * 신청 URL이 있는 정책만 조회
     */
    List<YouthPolicy> findByApplyUrlIsNotNullAndApplyUrlNot(String emptyUrl);

    /**
     * 최근 등록순으로 정렬하여 조회
     */
    List<YouthPolicy> findTop10ByOrderByCreateDateDesc();

    /**
     * 조회수 기준 인기 정책 조회
     */
    List<YouthPolicy> findTop10ByInquiryCountIsNotNullOrderByInquiryCountDesc();

    /**
     * 제목으로 정확히 일치하는 정책 검색
     */
    YouthPolicy findByTitle(String title);

    /**
     * 키워드가 포함된 정책 검색 (단일 키워드)
     */
    List<YouthPolicy> findByKeywordsContaining(String keyword);

    /**
     * 지원 내용에서 특정 단어가 포함된 정책 검색
     */
    List<YouthPolicy> findBySupportContentContaining(String content);

    /**
     * 특정 기간에 등록된 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE yp.createDate BETWEEN :startDate AND :endDate ORDER BY yp.createDate DESC")
    List<YouthPolicy> findByCreateDateBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 복합 검색 (제목, 키워드, 기관, 설명)
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE " +
            "(:keyword IS NULL OR yp.title LIKE %:keyword% OR yp.keywords LIKE %:keyword% OR yp.description LIKE %:keyword%) AND "
            +
            "(:institution IS NULL OR yp.supervisingInstitution LIKE %:institution%) AND " +
            "(:category IS NULL OR yp.largeClassification LIKE %:category% OR yp.mediumClassification LIKE %:category%)")
    List<YouthPolicy> findByComplexSearch(@Param("keyword") String keyword,
            @Param("institution") String institution,
            @Param("category") String category);

    /**
     * 페이징을 포함한 복합 검색
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE " +
            "(:keyword IS NULL OR yp.title LIKE %:keyword% OR yp.keywords LIKE %:keyword% OR yp.description LIKE %:keyword%) AND "
            +
            "(:institution IS NULL OR yp.supervisingInstitution LIKE %:institution%) AND " +
            "(:category IS NULL OR yp.largeClassification LIKE %:category% OR yp.mediumClassification LIKE %:category%)")
    Page<YouthPolicy> findByComplexSearchPaged(@Param("keyword") String keyword,
            @Param("institution") String institution,
            @Param("category") String category,
            Pageable pageable);

    /**
     * 기관별 정책 수 통계
     */
    @Query("SELECT yp.supervisingInstitution, COUNT(yp) FROM YouthPolicy yp WHERE yp.supervisingInstitution IS NOT NULL GROUP BY yp.supervisingInstitution ORDER BY COUNT(yp) DESC")
    List<Object[]> countBySupervisingInstitution();

    /**
     * 중분류별 정책 수 통계
     */
    @Query("SELECT yp.mediumClassification, COUNT(yp) FROM YouthPolicy yp WHERE yp.mediumClassification IS NOT NULL GROUP BY yp.mediumClassification ORDER BY COUNT(yp) DESC")
    List<Object[]> countByMediumClassification();

    /**
     * 신청 기간이 설정된 정책만 조회
     */
    List<YouthPolicy> findByApplicationStartDateIsNotNullAndApplicationEndDateIsNotNull();

    /**
     * 특정 연도에 등록된 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE YEAR(yp.createDate) = :year")
    List<YouthPolicy> findByCreateDateYear(@Param("year") int year);

    /**
     * 특정 월에 등록된 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE YEAR(yp.createDate) = :year AND MONTH(yp.createDate) = :month")
    List<YouthPolicy> findByCreateDateYearAndMonth(@Param("year") int year, @Param("month") int month);

    /**
     * 참고 URL이 있는 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE yp.referenceUrl1 IS NOT NULL OR yp.referenceUrl2 IS NOT NULL")
    List<YouthPolicy> findPoliciesWithReferenceUrls();

    /**
     * 지원 규모가 명시된 정책 조회
     */
    List<YouthPolicy> findBySupportScaleIsNotNull();

    /**
     * 특정 문자열이 포함된 지원 내용 검색
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE yp.supportContent LIKE %:searchTerm%")
    List<YouthPolicy> findBySupportContentContainsIgnoreCase(@Param("searchTerm") String searchTerm);

    /**
     * 빈 값이 아닌 신청 방법이 있는 정책 조회
     */
    List<YouthPolicy> findByApplyMethodContentIsNotNull();

    /**
     * 특정 기간 내 신청 가능한 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE " +
            "yp.applicationStartDate <= :endDate AND yp.applicationEndDate >= :startDate")
    List<YouthPolicy> findPoliciesAvailableInPeriod(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 조회수가 특정 값 이상인 정책 조회
     */
    List<YouthPolicy> findByInquiryCountGreaterThanEqual(Integer minInquiryCount);

    /**
     * 최근 업데이트된 정책 조회 (상위 N개)
     */
    List<YouthPolicy> findTop20ByOrderByUpdateDateDesc();

    /**
     * 데이터 정합성 체크를 위한 중복 제목 조회
     */
    @Query("SELECT yp.title, COUNT(yp) FROM YouthPolicy yp GROUP BY yp.title HAVING COUNT(yp) > 1")
    List<Object[]> findDuplicateTitles();

    /**
     * 필수 정보가 누락된 정책 조회 (데이터 품질 체크용)
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE " +
            "yp.title IS NULL OR yp.title = '' OR " +
            "yp.description IS NULL OR yp.description = '' OR " +
            "yp.supervisingInstitution IS NULL OR yp.supervisingInstitution = ''")
    List<YouthPolicy> findPoliciesWithMissingInfo();

    /**
     * 전체 정책의 평균 조회수
     */
    @Query("SELECT AVG(yp.inquiryCount) FROM YouthPolicy yp WHERE yp.inquiryCount IS NOT NULL")
    Double getAverageInquiryCount();

    /**
     * 가장 많이 조회된 정책의 조회수
     */
    @Query("SELECT MAX(yp.inquiryCount) FROM YouthPolicy yp WHERE yp.inquiryCount IS NOT NULL")
    Integer getMaxInquiryCount();

    /**
     * 신청 마감이 임박한 정책 조회 (N일 이내)
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE " +
            "yp.applicationEndDate IS NOT NULL AND " +
            "yp.applicationEndDate BETWEEN :now AND :deadlineDate " +
            "ORDER BY yp.applicationEndDate ASC")
    List<YouthPolicy> findPoliciesWithUpcomingDeadline(@Param("now") LocalDateTime now,
            @Param("deadlineDate") LocalDateTime deadlineDate);

    /**
     * 특정 키워드가 제목에 포함된 정책 수 카운트
     */
    long countByTitleContaining(String keyword);

    /**
     * 특정 기관의 정책 수 카운트
     */
    long countBySupervisingInstitutionContaining(String institution);

    /**
     * 현재 진행중인 정책 중 나이 제한이 없는 정책
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE " +
            "yp.applicationStartDate <= :now AND yp.applicationEndDate >= :now AND " +
            "(yp.minAge IS NULL AND yp.maxAge IS NULL)")
    List<YouthPolicy> findCurrentPoliciesWithoutAgeLimit(@Param("now") LocalDateTime now);
}