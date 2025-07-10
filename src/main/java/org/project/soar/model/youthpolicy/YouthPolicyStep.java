package org.project.soar.model.youthpolicy;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "youth_policy_step")
@Getter
@NoArgsConstructor
public class YouthPolicyStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") 
    private Long id;

    @Column(name = "policy_id", length = 50, nullable = false)
    private String policyId;

    @Column(name = "submitted_documents", columnDefinition = "TEXT")
    private String submittedDocuments;

    @Column(name = "step1", columnDefinition = "TEXT")
    private String step1;

    @Column(name = "step2", columnDefinition = "TEXT")
    private String step2;

    @Column(name = "step3", columnDefinition = "TEXT")
    private String step3;

    @Column(name = "step4", columnDefinition = "TEXT")
    private String step4;

    @Column(name = "caution", columnDefinition = "TEXT")
    private String caution;

    @Builder
    public YouthPolicyStep(String policyId, String submittedDocuments, String step1, String step2,
            String step3, String step4, String caution) {
        this.policyId = policyId;
        this.submittedDocuments = submittedDocuments;
        this.step1 = step1;
        this.step2 = step2;
        this.step3 = step3;
        this.step4 = step4;
        this.caution = caution;
    }
}
