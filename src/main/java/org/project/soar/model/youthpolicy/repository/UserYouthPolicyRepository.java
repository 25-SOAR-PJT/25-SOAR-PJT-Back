package org.project.soar.model.youthpolicy.repository;
import org.project.soar.model.user.User;
import org.project.soar.model.youthpolicy.UserYouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}

