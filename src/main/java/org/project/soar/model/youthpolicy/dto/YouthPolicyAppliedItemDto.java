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
public class YouthPolicyAppliedItemDto {
    private String policyId;
    private String policyName;
    private String dateLabel; // 예: D-3, 신청 마감, 사업 종료 등
    private String dateType;  // ONGOING / DEADLINE / FINISHED / UPCOMING

    public static YouthPolicyAppliedItemDto from(YouthPolicy p) {
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

        return YouthPolicyAppliedItemDto.builder()
                .policyId(p.getPolicyId())
                .policyName(p.getPolicyName())
                .dateLabel(r.label())
                .dateType(r.type())
                .build();
    }
}