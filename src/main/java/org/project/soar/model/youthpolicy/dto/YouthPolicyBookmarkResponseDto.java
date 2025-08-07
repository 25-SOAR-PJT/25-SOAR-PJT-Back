package org.project.soar.model.youthpolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.util.DateClassifier;

@Getter
@AllArgsConstructor
@Builder
public class YouthPolicyBookmarkResponseDto {

    private String policyId; // String으로 그대로 유지
    private String policyName;
    private String dateLabel;

    public static YouthPolicyBookmarkResponseDto from(YouthPolicy policy) {
        DateClassifier.DateResult result = DateClassifier.classify(
            policy.getApplicationStartDate() != null ? policy.getApplicationStartDate().toLocalDate() : null,
            policy.getApplicationEndDate() != null ? policy.getApplicationEndDate().toLocalDate() : null,
            policy.getBusinessPeriodEnd(),
            policy.getPolicySupportContent(),           
            policy.getApplyMethodContent(),
            policy.getScreeningMethodContent(),
            policy.getBusinessPeriodEtc(),
            policy.getPolicyName(),                   
            LocalDate.now()                           
        );
        
        return YouthPolicyBookmarkResponseDto.builder()
                .policyId(policy.getPolicyId())
                .policyName(policy.getPolicyName())
                .dateLabel(result.label()) // 계산된 라벨 사용
                .build();
    }
}
