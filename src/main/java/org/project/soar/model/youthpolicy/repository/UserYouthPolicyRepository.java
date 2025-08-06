package org.project.soar.model.youthpolicy.repository;
import io.lettuce.core.dynamic.annotation.Param;
import org.project.soar.model.user.User;
import org.project.soar.model.youthpolicy.UserYouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserYouthPolicyRepository extends JpaRepository<UserYouthPolicy, Long> {

    void deleteAllByUser(User user);

    /**
     * 특정 사용자와 정책으로 신청 여부 확인 (중복 방지용)
     */
    Optional<UserYouthPolicy> findByUserAndPolicy(User user, YouthPolicy policy);

    /**
     * 특정 사용자가 신청한 모든 정책 조회
     */
    List<UserYouthPolicy> findByUser(User user);

    /**
     * 특정 정책에 신청한 사용자 수 조회
     */
    long countByPolicy(YouthPolicy policy);

    /**
     * 특정 사용자와 정책 조합이 존재하는지 여부
     */
    boolean existsByUserAndPolicy(User user, YouthPolicy policy);

    /**
     * 실시간 인기 지원사업
     */
    @Query("""
        SELECT p
        FROM UserYouthPolicy uyp
        JOIN uyp.policy p
        GROUP BY p
        ORDER BY COUNT(uyp) DESC
    """)
    List<YouthPolicy> findTop10ByApplicationCount(Pageable pageable);

    /**
     * 나이대별 실시간 인기 지원사업
     */
    @Query(value = """
        SELECT p.*
        FROM user_youth_policy uyp
        JOIN user u ON uyp.user_id = u.user_id
        JOIN youth_policy p ON uyp.policy_id = p.policy_id
        WHERE u.user_birth_date IS NOT NULL
            AND FLOOR(TIMESTAMPDIFF(YEAR, u.user_birth_date, CURDATE()) / 10) * 10 = :ageGroup
        GROUP BY p.policy_id
        ORDER BY COUNT(*) DESC
    """, nativeQuery = true)
    List<YouthPolicy> findTop10PopularByAgeGroup(@Param("ageGroup") int ageGroup, Pageable pageable);

}

