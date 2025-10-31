package org.project.soar.model.alarm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, String> {

    Optional<FcmToken> findByUserId(Long userId);

//    Optional<List<FcmToken>> findAllByAlarmEnabledTrue();

    Optional<FcmToken> findAllByAlarmEnabledAllTrue();
}
