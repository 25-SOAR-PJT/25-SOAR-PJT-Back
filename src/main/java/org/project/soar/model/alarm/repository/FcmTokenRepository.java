package org.project.soar.model.alarm.repository;

import org.project.soar.model.alarm.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, String> {

    Optional<FcmToken> findByUserId(Long userId);
}
