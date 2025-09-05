package org.project.soar.model.permission;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.project.soar.model.user.User;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "permission") // 앱 권한 및 약관 동의 테이블
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id", nullable = false) // 권한 ID
    private Long permissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // 사용자 ID
    private User user;

    @Column(name = "type", nullable = false, length = 50) // 약관 유형 또는 권한 유형
    private String type;

    @Column(name = "status", nullable = false) // 동의 여부: true = 동의, false = 미동의
    private boolean status;

    @Builder
    public Permission(Long permissionId, User user, String type, boolean status) {
        this.permissionId = permissionId;
        this.user = user;
        this.type = type;
        this.status = status;
    }
}
