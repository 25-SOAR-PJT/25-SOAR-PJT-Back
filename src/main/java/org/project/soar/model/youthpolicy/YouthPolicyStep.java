package org.project.soar.model.youthpolicy;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "youth_policy_step")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicyStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_id", length = 50, nullable = false)
    private String policyId;

    @Column(columnDefinition = "TEXT")
    private String submittedDocuments;

    @Column(columnDefinition = "TEXT")
    private String applyStep;

    @Column(columnDefinition = "TEXT")
    private String documentStep;

    @Column(columnDefinition = "TEXT")
    private String noticeStep;

    @Column(columnDefinition = "TEXT")
    private String caution;
}
