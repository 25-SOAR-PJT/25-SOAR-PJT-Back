package org.project.soar.model.youthpolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.util.DateClassifier;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class YouthPolicyEndDateItemDto {
    private String policyId;
    private String policyName;
    private String dateLabel; // 예: 신청 마감일 D-3
    private String businessPeriodEnd;

    public static YouthPolicyEndDateItemDto from(YouthPolicy p) {
        var r = DateClassifier.classify(
                p.getApplicationStartDate() == null ? null : p.getApplicationStartDate().toLocalDate(),
                p.getApplicationEndDate()   == null ? null : p.getApplicationEndDate().toLocalDate(),
                p.getBusinessPeriodEnd(),
                p.getPolicySupportContent(),
                p.getApplyMethodContent(),
                p.getScreeningMethodContent(),
                p.getBusinessPeriodEtc(),
                p.getPolicyName(),
                LocalDate.now()
        );

        return YouthPolicyEndDateItemDto.builder()
                .policyId(p.getPolicyId())
                .policyName(p.getPolicyName())
                .dateLabel(r.label())
                .businessPeriodEnd(p.getBusinessPeriodEnd())
                .build();
    }
}