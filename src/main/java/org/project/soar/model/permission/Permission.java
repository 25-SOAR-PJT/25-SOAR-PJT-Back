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
@Table(name = "permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "status", nullable = false)
    private boolean status;

    @Builder
    public Permission(Long permissionId, User user, String type, boolean status) {
        this.permissionId = permissionId;
        this.user = user;
        this.type = type;
        this.status = status;
    }

    // ✨ 새로운 업데이트 메서드 추가
    public void updateStatus(boolean newStatus) {
        this.status = newStatus;
    }
}