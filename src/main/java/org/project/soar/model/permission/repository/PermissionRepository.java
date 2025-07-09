package org.project.soar.model.permission.repository;

import org.project.soar.model.permission.Permission;
import org.project.soar.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // 약관 동의 확인 (status를 Boolean으로 변경)
    boolean existsByUserAndTypeAndStatus(User user, String type, Boolean status);

    // 사용자와 관련된 모든 약관 조회
    List<Permission> findByUser(User user);

    void deleteAllByUser(User user);

}
