package org.project.soar.model.permission.service;

import jakarta.transaction.Transactional;
import org.project.soar.model.permission.Permission;
import org.project.soar.model.permission.repository.PermissionRepository;
import org.project.soar.model.user.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }


    private static final String[] TERM_TYPES =
            {"필수약관1", "필수약관2", "선택약관1", "선택약관2", "필수약관3"};

    public void saveAgreedTerms(User user, List<Boolean> agreedTerms) {
        for (int i = 0; i < TERM_TYPES.length; i++) {
            boolean status = (i < agreedTerms.size()) && Boolean.TRUE.equals(agreedTerms.get(i));
            Permission permission = Permission.builder()
                    .user(user)
                    .type(TERM_TYPES[i])
                    .status(status)
                    .build();
            permissionRepository.save(permission);
        }
    }

    /** 필수 약관 동의 여부를 리스트만 보고 판단 (DB 조회 불필요) */
    public boolean isAllRequiredAgreed(List<Boolean> agreedTerms) {
        // 필수 인덱스: 0, 1, 4  (TERM_TYPES와 프런트의 순서가 현재 일치)
        return get(agreedTerms, 0) && get(agreedTerms, 1) && get(agreedTerms, 4);
    }

    private boolean get(List<Boolean> list, int idx) {
        return idx < list.size() && Boolean.TRUE.equals(list.get(idx));
    }

    public boolean hasAgreedToRequiredTerms(User user) {
        List<Permission> permissions = permissionRepository.findByUser(user);
        // 필수 약관이 모두 동의되어야만 true 반환
        return permissions.stream()
                .filter(permission -> permission.getType().contains("필수"))
                .allMatch(Permission::isStatus);
    }

    /** ✨ "선택약관2" 동의를 추가하거나 업데이트하는 새로운 메서드 */
    @Transactional
    public String agreeToOptionalTerm2(User user) {
        final String OPTIONAL_TERM_2 = "선택약관2";

        Optional<Permission> existingPermissionOpt = permissionRepository.findByUserAndType(user, OPTIONAL_TERM_2);

        if (existingPermissionOpt.isPresent()) {
            // 이미 약관 정보가 존재하는 경우
            Permission existingPermission = existingPermissionOpt.get();
            if (existingPermission.isStatus()) {
                return "이미 동의한 약관입니다.";
            } else {
                // 미동의 상태였다면 동의로 변경
                existingPermission.updateStatus(true);
                permissionRepository.save(existingPermission);
                return "약관 동의가 완료되었습니다.";
            }
        } else {
            // 약관 정보가 없는 경우 새로 생성
            Permission permission = Permission.builder()
                    .user(user)
                    .type(OPTIONAL_TERM_2)
                    .status(true)
                    .build();
            permissionRepository.save(permission);
            return "약관 동의가 완료되었습니다.";
        }
    }

}
