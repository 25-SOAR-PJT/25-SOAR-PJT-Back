package org.project.soar.model.youthpolicy;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.project.soar.model.user.User;

@Entity
@Table(name = "user_youth_policy", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "policy_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserYouthPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", referencedColumnName = "policy_id", nullable = false)
    private YouthPolicy youthPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PolicyStatus status = PolicyStatus.SAVED;

    @Column(name = "applied_yn", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean appliedYn = false;

    @Column(name = "applied_date")
    private LocalDateTime appliedDate;

    @Column(name = "archived_yn", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean archivedYn = false;

    @Column(name = "archived_date")
    private LocalDateTime archivedDate;

    @Column(name = "interest_score")
    private Integer interestScore;

    @Column(name = "memo", length = 1000)
    private String memo;

    @Column(name = "notification_yn", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private Boolean notificationYn = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum PolicyStatus {
        SAVED("저장됨"),
        APPLIED("지원완료"),
        INTERESTED("관심있음"),
        NOT_INTERESTED("관심없음"),
        ARCHIVED("보관됨");

        private final String description;

        PolicyStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}