package org.project.soar.model.youthpolicy.repository;
import org.project.soar.model.user.User;
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

    /**
     * 정책명으로 검색 (키워드 검색용)
     */
    List<YouthPolicy> findByPolicyNameContaining(String keyword);

    @Query("SELECT y FROM YouthPolicy y WHERE " +
                  "LOWER(y.policyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                  "LOWER(y.policyExplanation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                  "LOWER(y.applyMethodContent) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                  "LOWER(y.submitDocumentContent) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                  "LOWER(y.additionalApplyQualification) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<YouthPolicy> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 정책아이디로 검색
     */
    YouthPolicy findByPolicyId(String policyId);

    /**
     * 대분류로 검색 (카테고리 검색용)
     */
    List<YouthPolicy> findByLargeClassificationContaining(String category);

    /**
     * 생성일 기준 검색 (최근 정책 동기화용)
     */
    long countByCreatedAtAfter(LocalDateTime cutoffDate);

    /**
     * 생성일 기준 내림차순 페이징
     */
    Page<YouthPolicy> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 정책 ID 존재 여부 확인
     */
    boolean existsByPolicyId(String policyId);

    /**
     * 정책 키워드로 검색
     */
    List<YouthPolicy> findByPolicyKeywordContaining(String keyword);

    /**
     * 정책 설명으로 검색
     */
    List<YouthPolicy> findByPolicyExplanationContaining(String keyword);

    /**
     * 복합 검색 (정책명, 키워드, 설명)
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE " +
           "yp.policyName LIKE %:keyword% OR " +
           "yp.policyKeyword LIKE %:keyword% OR " +
           "yp.policyExplanation LIKE %:keyword%")
    List<YouthPolicy> findByKeywordInMultipleFields(@Param("keyword") String keyword);

    /**
     * 중분류로 검색
     */
    List<YouthPolicy> findByMediumClassificationContaining(String category);

    /**
     * 관리기관으로 검색
     */
    List<YouthPolicy> findBySupervisingInstNameContaining(String institution);

    /**
     * 나이 조건에 맞는 정책 검색
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE " +
           "(yp.supportTargetMinAge IS NULL OR yp.supportTargetMinAge <= :age) AND " +
           "(yp.supportTargetMaxAge IS NULL OR yp.supportTargetMaxAge >= :age)")
    List<YouthPolicy> findPoliciesForAge(@Param("age") Integer age);

    /**
     * 신청 URL이 있는 정책만 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE yp.applyUrl IS NOT NULL AND yp.applyUrl != ''")
    List<YouthPolicy> findPoliciesWithApplyUrl();

    /**
     * 최근 등록된 정책 조회 (상위 N개)
     */
    List<YouthPolicy> findTop10ByOrderByCreatedAtDesc();

    /**
     * 조회수 기준 인기 정책 조회 (상위 N개)
     */
    List<YouthPolicy> findTop10ByInquiryCountIsNotNullOrderByInquiryCountDesc();

    /**
     * 특정 기간에 등록된 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE yp.createdAt BETWEEN :startDate AND :endDate ORDER BY yp.createdAt DESC")
    List<YouthPolicy> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * 페이징된 복합 검색
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE " +
           "(:keyword IS NULL OR yp.policyName LIKE %:keyword% OR yp.policyKeyword LIKE %:keyword% OR yp.policyExplanation LIKE %:keyword%) AND " +
           "(:category IS NULL OR yp.largeClassification LIKE %:category% OR yp.mediumClassification LIKE %:category%)")
    Page<YouthPolicy> findByKeywordAndCategoryPaged(@Param("keyword") String keyword,
                                                    @Param("category") String category,
                                                    Pageable pageable);

    /**
     * 대분류별 정책 수 통계
     */
    @Query("SELECT yp.largeClassification, COUNT(yp) FROM YouthPolicy yp WHERE yp.largeClassification IS NOT NULL AND yp.largeClassification != '' GROUP BY yp.largeClassification ORDER BY COUNT(yp) DESC")
    List<Object[]> countByLargeClassificationGrouped();

    /**
     * 관리기관별 정책 수 통계
     */
    @Query("SELECT yp.supervisingInstName, COUNT(yp) FROM YouthPolicy yp WHERE yp.supervisingInstName IS NOT NULL AND yp.supervisingInstName != '' GROUP BY yp.supervisingInstName ORDER BY COUNT(yp) DESC")
    List<Object[]> countBySupervisingInstNameGrouped();

    /**
     * 월별 등록 통계 (최근 N개월)
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', yp.createdAt, '%Y-%m') as month, COUNT(yp) " +
           "FROM YouthPolicy yp " +
           "WHERE yp.createdAt >= :fromDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', yp.createdAt, '%Y-%m') " +
           "ORDER BY month DESC")
    List<Object[]> countByCreatedAtGroupByMonth(@Param("fromDate") LocalDateTime fromDate);

    /**
     * 조회수가 특정 값 이상인 정책 조회
     */
    List<YouthPolicy> findByInquiryCountGreaterThanEqual(Integer minInquiryCount);

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
     * 특정 키워드가 정책명에 포함된 정책 수 카운트
     */
    long countByPolicyNameContaining(String keyword);

    /**
     * 특정 기관의 정책 수 카운트
     */
    long countBySupervisingInstNameContaining(String institution);

    /**
     * 특정 분류의 정책 수 카운트
     */
    long countByLargeClassificationContaining(String classification);

    /**
     * 지원 대상 연령이 설정된 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE yp.supportTargetMinAge IS NOT NULL OR yp.supportTargetMaxAge IS NOT NULL")
    List<YouthPolicy> findPoliciesWithAgeLimit();

    /**
     * 지원 대상 연령이 설정되지 않은 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE yp.supportTargetMinAge IS NULL AND yp.supportTargetMaxAge IS NULL")
    List<YouthPolicy> findPoliciesWithoutAgeLimit();

    /**
     * 정책 지원 내용으로 검색
     */
    List<YouthPolicy> findByPolicySupportContentContaining(String content);

    /**
     * 신청 방법이 명시된 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE yp.applyMethodContent IS NOT NULL AND yp.applyMethodContent != ''")
    List<YouthPolicy> findPoliciesWithApplyMethod();

    /**
     * 참고 URL이 있는 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE yp.referenceUrl1 IS NOT NULL OR yp.referenceUrl2 IS NOT NULL")
    List<YouthPolicy> findPoliciesWithReferenceUrls();

    /**
     * 필수 정보가 누락된 정책 조회 (데이터 품질 체크용)
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE " +
           "yp.policyName IS NULL OR yp.policyName = '' OR " +
           "yp.policyExplanation IS NULL OR yp.policyExplanation = ''")
    List<YouthPolicy> findPoliciesWithMissingInfo();

    /**
     * 최근 수정된 정책 조회 (상위 N개)
     */
    List<YouthPolicy> findTop20ByOrderByUpdatedAtDesc();

    /**
     * 특정 연도에 등록된 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE YEAR(yp.createdAt) = :year")
    List<YouthPolicy> findByCreatedAtYear(@Param("year") int year);

    /**
     * 특정 연월에 등록된 정책 조회
     */
    @Query("SELECT yp FROM YouthPolicy yp WHERE YEAR(yp.createdAt) = :year AND MONTH(yp.createdAt) = :month")
    List<YouthPolicy> findByCreatedAtYearAndMonth(@Param("year") int year, @Param("month") int month);

    List<YouthPolicy> findTop2ByOrderByCreatedAtDesc();

    List<YouthPolicy> findTop5ByOrderByCreatedAtDesc();

    List<YouthPolicy> findTop20ByOrderByCreatedAtDesc();

    List<YouthPolicy> findTop10ByOrderByCreatedAtAsc();

    /**
     * 마감임박 지원사업 리스트
     */
    @Query("""
        SELECT p
        FROM YouthPolicy p
        WHERE p.businessPeriodEnd IS NOT NULL
          AND TRIM(p.businessPeriodEnd) <> ''
          AND FUNCTION('STR_TO_DATE', p.businessPeriodEnd, '%Y%m%d') >= CURRENT_DATE
        ORDER BY p.businessPeriodEnd ASC
    """)
    Page<YouthPolicy> findLatestPoliciesByEndDate(Pageable pageable);
}

