package org.project.soar.model.youthpolicy.dto;

import lombok.Builder;
import lombok.Data;

import java.util.stream.Collectors;

@Data
public class YouthPolicyOpenAI {
    private String policyId;
    private String policyName;//project_name
    private String policyExplanation;//support_summary
    private String policySupportContent;
    private Integer supportTargetMinAge;
    private Integer supportTargetMaxAge;
    private String supportTargetAgeLimitYn;
    @Builder
    public YouthPolicyOpenAI(String policyId, String policyName, String policyExplanation, String policySupportContent, Integer supportTargetMinAge, Integer supportTargetMaxAge, String supportTargetAgeLimitYn) {
        this.policyId = policyId;
        this.policyName = policyName;
        this.policyExplanation = policyExplanation;
        this.policySupportContent = policySupportContent;
        this.supportTargetMinAge = supportTargetMinAge;
        this.supportTargetMaxAge = supportTargetMaxAge;
        this.supportTargetAgeLimitYn = supportTargetAgeLimitYn;
    }
}
