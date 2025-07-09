package org.project.soar.model.permission.service;

import org.project.soar.model.permission.Permission;
import org.project.soar.model.permission.repository.PermissionRepository;
import org.project.soar.model.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public void saveAgreedTerms(User user, List<Boolean> agreedTerms) {
        // 필수 약관, 선택 약관 순서로 동의 여부 저장
        String[] termTypes = { "필수약관1", "필수약관2", "선택약관1","선택약관2" };

        for (int i = 0; i < termTypes.length; i++) {
            if (i < agreedTerms.size()) {
                Permission permission = Permission.builder()
                        .user(user)
                        .type(termTypes[i])
                        .status(agreedTerms.get(i)) // 동의 여부 저장
                        .build();
                permissionRepository.save(permission);
            }
        }
    }

    public boolean hasAgreedToRequiredTerms(User user) {
        List<Permission> permissions = permissionRepository.findByUser(user);
        // 필수 약관이 모두 동의되어야만 true 반환
        return permissions.stream()
                .filter(permission -> permission.getType().contains("필수"))
                .allMatch(Permission::isStatus);
    }
}
