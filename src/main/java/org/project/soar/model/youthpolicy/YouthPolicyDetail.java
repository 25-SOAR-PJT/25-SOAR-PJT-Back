package org.project.soar.model.youthpolicy;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "youth_policy_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicyDetail {

    @Id
    @Column(name = "policy_id", length = 50)
    private String policyId;

    @Column(name = "info_detail", columnDefinition = "TEXT")
    private String infoDetail;

    @Column(name = "target_detail", columnDefinition = "TEXT")
    private String targetDetail;

    @Column(name = "support_detail", columnDefinition = "TEXT")
    private String supportDetail;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "policy_id", referencedColumnName = "policy_id")
    private YouthPolicy youthPolicy;
}