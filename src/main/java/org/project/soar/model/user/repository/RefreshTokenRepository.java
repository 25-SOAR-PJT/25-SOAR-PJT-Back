package org.project.soar.model.user.repository;

import org.project.soar.model.user.RefreshToken;
import org.project.soar.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

}
