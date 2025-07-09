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
    private Long id;
    
    @Column(name = "policy_id", length = 50)
    private String policyId; // plcyNo

    private String submittedDocuments;
    private String step1;
    private String step2;
    private String step3;
    private String step4;

    @Builder
    public YouthPolicyStep(String policyId, String submittedDocuments, String step1, String step2, String step3,
            String step4) {
        this.policyId = policyId;
        this.submittedDocuments = submittedDocuments;
        this.step1 = step1;
        this.step2 = step2;
        this.step3 = step3;
        this.step4 = step4;
    }
}
