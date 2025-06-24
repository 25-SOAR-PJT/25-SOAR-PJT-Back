package org.project.soar.model.youthpolicy;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.project.soar.model.user.User;

@Entity
@Table(name = "UserYouthPolicy")
@Data
public class UserYouthPolicy {
    @EmbeddedId
    private UserYouthPolicyId id;

    @Column(name = "applied_yn")
    private Boolean appliedYn;

    @Column(name = "archivedDate")
    private LocalDateTime archivedDate;

    @Column(name = "delete_yn")
    private Boolean deleteYn;

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private YouthPolicy youthPolicy;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
}

@Embeddable
@Data
class UserYouthPolicyId implements Serializable {
    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "user_id")
    private Integer userId;
}
