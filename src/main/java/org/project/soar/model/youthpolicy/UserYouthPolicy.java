package org.project.soar.model.youthpolicy;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.project.soar.model.user.User;

@Entity
@Table(name = "user_youth_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserYouthPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", referencedColumnName = "policy_id")
    private YouthPolicy policy;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;
}
