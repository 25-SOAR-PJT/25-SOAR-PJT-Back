package org.project.soar.model.permission;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import org.project.soar.model.user.User;
import java.io.Serializable;

@Entity
@Table(name = "Permission")
@Data
public class Permission {
    @EmbeddedId
    private PermissionId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
}

@Embeddable
@Data
class PermissionId implements Serializable {
    @Column(name = "permission_id")
    private String permissionId;

    @Column(name = "user_id")
    private Integer userId;
}
