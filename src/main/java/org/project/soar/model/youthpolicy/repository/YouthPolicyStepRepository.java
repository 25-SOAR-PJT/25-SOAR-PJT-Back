package org.project.soar.model.youthpolicy.repository;

import java.util.Optional;

import org.project.soar.model.youthpolicy.YouthPolicyStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YouthPolicyStepRepository extends JpaRepository<YouthPolicyStep, Long> {
    Optional<YouthPolicyStep> findByPolicyId(String policyId);

}
