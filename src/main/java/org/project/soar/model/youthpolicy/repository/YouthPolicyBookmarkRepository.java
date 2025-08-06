package org.project.soar.model.youthpolicy.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.project.soar.model.user.User;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicyBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

}
