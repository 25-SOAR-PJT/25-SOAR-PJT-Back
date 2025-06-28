package org.project.soar.model.permission;

import jakarta.persistence.*;
import lombok.*;
import org.project.soar.model.user.User;
import org.project.soar.global.abstracts.BaseTimeEntity;

@Entity
@Table(name = "permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;

    @Column(name = "permission_key", length = 50, nullable = false)
    private String permissionKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
