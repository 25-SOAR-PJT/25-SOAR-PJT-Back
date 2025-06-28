package org.project.soar.model.youthpolicy;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.project.soar.model.user.User;

@Entity
@Table(name = "user_youth_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserYouthPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "applied_yn")
    private Boolean appliedYn;

    @Column(name = "archivedDate")
    private LocalDateTime archivedDate;

    @Column(name = "delete_yn")
    private Boolean deleteYn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", referencedColumnName = "policy_id")
    private YouthPolicy youthPolicy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}

@Embeddable
@Data
class UserYouthPolicyId implements Serializable {
    @Column(name = "project_id")
    private String projectId;

    @Column(name = "user_id")
    private String userId;
}
