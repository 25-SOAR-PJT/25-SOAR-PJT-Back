package org.project.soar.model.tag.repository;

import org.project.soar.model.tag.YouthPolicyTag;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface YouthPolicyTagRepository extends JpaRepository<YouthPolicyTag, Long> {

    boolean existsByYouthPolicy(YouthPolicy youthPolicy);
}
