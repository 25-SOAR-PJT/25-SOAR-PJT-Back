package org.project.soar.model.user.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.project.soar.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.userEmail) = LOWER(:userEmail)")
    Optional<User> findByUserEmailOptional(@Param("userEmail") String userEmail);

    boolean existsByUserName(String userUsername);

    boolean existsByUserEmail(String userEmail);

    Optional<User> findByUserId(Long userId);
}
