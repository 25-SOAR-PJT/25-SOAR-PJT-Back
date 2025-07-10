package org.project.soar.model.youthpolicy.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class YouthPolicyOpenAI {
    private String policyId;
    private String policyName;//project_name
    private String policyExplanation;//support_summary
    private String policySupportContent;
    private String applyMethodContent;
    private String screeningMethodContent;
    private String submitDocumentContent;
    private Integer supportTargetMinAge;
    private Integer supportTargetMaxAge;
    private String supportTargetAgeLimitYn;
    @Builder
    public YouthPolicyOpenAI(String policyId, String policyName, String policyExplanation, String policySupportContent, String applyMethodContent, String screeningMethodContent, String submitDocumentContent, Integer supportTargetMinAge, Integer supportTargetMaxAge, String supportTargetAgeLimitYn) {
        this.policyId = policyId;
        this.policyName = policyName;
        this.policyExplanation = policyExplanation;
        this.policySupportContent = policySupportContent;
        this.applyMethodContent = applyMethodContent;
        this.screeningMethodContent = screeningMethodContent;
        this.submitDocumentContent = submitDocumentContent;
        this.supportTargetMinAge = supportTargetMinAge;
        this.supportTargetMaxAge = supportTargetMaxAge;
        this.supportTargetAgeLimitYn = supportTargetAgeLimitYn;
    }
}