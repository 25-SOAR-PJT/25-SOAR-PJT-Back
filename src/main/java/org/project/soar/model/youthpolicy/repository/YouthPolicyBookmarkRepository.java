package org.project.soar.model.youthpolicy.repository;

import org.project.soar.model.user.User;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicyBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface YouthPolicyBookmarkRepository extends JpaRepository<YouthPolicyBookmark, Long> {
    Optional<YouthPolicyBookmark> findByUserAndPolicy(User user, YouthPolicy policy);

    List<YouthPolicyBookmark> findAllByUser(User user);

    void deleteByUserAndPolicy(User user, YouthPolicy policy);
}
