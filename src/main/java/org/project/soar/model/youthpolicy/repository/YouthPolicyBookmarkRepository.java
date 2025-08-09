package org.project.soar.model.youthpolicy.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.project.soar.model.user.User;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicyBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface YouthPolicyBookmarkRepository extends JpaRepository<YouthPolicyBookmark, Long> {
    Optional<YouthPolicyBookmark> findByUserAndPolicy(User user, YouthPolicy policy);

    List<YouthPolicyBookmark> findAllByUser(User user);

    void deleteByUserAndPolicy(User user, YouthPolicy policy);

    @Query("""
        SELECT p
        FROM YouthPolicyBookmark b
        JOIN b.policy p
        WHERE b.user = :user
          AND p.businessPeriodEnd IS NOT NULL
          AND TRIM(p.businessPeriodEnd) <> ''
          AND FUNCTION('STR_TO_DATE', p.businessPeriodEnd, '%Y%m%d') >= CURRENT_DATE
        ORDER BY p.businessPeriodEnd ASC
    """)
    List<YouthPolicy> findLatestBookmarkByEndDate(@Param("user") User user);

    // 전체 인기 (북마크 수 높은 순) — Pageable로 Top N
    @Query("""
        SELECT p
        FROM YouthPolicyBookmark b
        JOIN b.policy p
        GROUP BY p
        ORDER BY COUNT(b) DESC
    """)
    List<YouthPolicy> findPopularByBookmarks(Pageable pageable);

    // 나이대별 인기 (북마크 기준) — native (MySQL)
    @Query(value = """
        SELECT p.*
        FROM user_policy_bookmark b
        JOIN user u ON b.user_id = u.user_id
        JOIN youth_policy p ON b.policy_id = p.policy_id
        WHERE u.user_birth_date IS NOT NULL
          AND FLOOR(TIMESTAMPDIFF(YEAR, u.user_birth_date, CURDATE()) / 10) * 10 = :ageGroup
        GROUP BY p.policy_id
        ORDER BY COUNT(*) DESC
        """, nativeQuery = true)
    List<YouthPolicy> findPopularBookmarksByAgeGroup(@Param("ageGroup") int ageGroup, Pageable pageable);


}
