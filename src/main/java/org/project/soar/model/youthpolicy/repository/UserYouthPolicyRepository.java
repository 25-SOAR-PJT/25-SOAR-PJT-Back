package org.project.soar.model.youthpolicy.repository;
import org.project.soar.model.user.User;
import org.project.soar.model.youthpolicy.UserYouthPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserYouthPolicyRepository extends JpaRepository<UserYouthPolicy, Long> {

    void deleteAllByUser(User user);
}

