package org.project.soar.model.youthpolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class YouthPolicyLatestResponseDto {
    private String policyId;
    private String policyName;
    private String businessPeriodEnd;
    private String dDay;

    @Builder
    public YouthPolicyLatestResponseDto(String policyId, String policyName, String businessPeriodEnd, String dDay) {
        this.policyId = policyId;
        this.policyName = policyName;
        this.businessPeriodEnd = businessPeriodEnd;
        this.dDay = dDay;
    }
}
