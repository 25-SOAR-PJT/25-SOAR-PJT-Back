package org.project.soar.model.category.repository;

import org.project.soar.model.category.Category;
import org.project.soar.model.category.dto.PopularPolicyDto;
import org.project.soar.model.user.User;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryCodeAndYouthPolicy(int categoryCode, YouthPolicy policy);

    /**
     * 카테고리별 인기 정책(북마크 수 DESC) + 요청 사용자 북마크 여부
     * - LEFT JOIN으로 북마크 0건도 포함
     * - Hibernate 기준 JPQL JOIN ... ON 사용
     */
    @Query("""
                SELECT new org.project.soar.model.category.dto.PopularPolicyDto(
                    p.policyId,
                    p.policyName,
                    c.categoryCode,
                    COUNT(DISTINCT bAll),
                    CASE WHEN COUNT(DISTINCT bMine) > 0 THEN true ELSE false END
                )
                  FROM Category c
                  JOIN c.youthPolicy p
                  LEFT JOIN org.project.soar.model.youthpolicy.YouthPolicyBookmark bAll
                         ON bAll.policy = p
                  LEFT JOIN org.project.soar.model.youthpolicy.YouthPolicyBookmark bMine
                         ON bMine.policy = p AND bMine.user = :user
                 WHERE c.categoryCode = :categoryCode
                 GROUP BY p.policyId, p.policyName, c.categoryCode
                 ORDER BY COUNT(DISTINCT bAll) DESC
            """)
    List<PopularPolicyDto> findPopularPoliciesByCategory(@Param("categoryCode") int categoryCode,
            @Param("user") User user,
            Pageable pageable);

    /**
     * 카테고리별 + 태그 필터 인기 정책(북마크 수 DESC) + 요청 사용자 북마크 여부
     */
    @Query("""
                SELECT new org.project.soar.model.category.dto.PopularPolicyDto(
                    p.policyId,
                    p.policyName,
                    c.categoryCode,
                    COUNT(DISTINCT bAll),
                    CASE WHEN COUNT(DISTINCT bMine) > 0 THEN true ELSE false END
                )
                  FROM Category c
                  JOIN c.youthPolicy p
                  JOIN org.project.soar.model.youthpolicytag.YouthPolicyTag ypt
                       ON ypt.youthPolicy = p
                  LEFT JOIN org.project.soar.model.youthpolicy.YouthPolicyBookmark bAll
                       ON bAll.policy = p
                  LEFT JOIN org.project.soar.model.youthpolicy.YouthPolicyBookmark bMine
                       ON bMine.policy = p AND bMine.user = :user
                 WHERE c.categoryCode = :categoryCode
                   AND ypt.tag.tagId IN :tagIds
                 GROUP BY p.policyId, p.policyName, c.categoryCode
                 ORDER BY COUNT(DISTINCT bAll) DESC
            """)
    List<PopularPolicyDto> findPopularPoliciesByCategoryAndTagIds(@Param("categoryCode") int categoryCode,
            @Param("tagIds") List<Long> tagIds,
            @Param("user") User user,
            Pageable pageable);
}
