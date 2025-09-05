package org.project.soar.model.youthpolicy;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "youth_policy_banner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicyBanner {

    @Id
    @Column(name = "policy_id", length = 50)
    private String policyId;

    @Column(name = "img", length = 3000)
    private String img;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "policy_id", referencedColumnName = "policy_id")
    private YouthPolicy youthPolicy;
}