package org.project.soar.model.youthpolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 배치 신청 항목별 결과 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicyBulkApplyItemResultDto {
    private String policyId;
    private ApplyStatus status; // APPLIED, ALREADY_APPLIED, APPLY_ENDED, BUSINESS_ENDED, OPEN_UPCOMING,
                                // NOT_FOUND
    private String message;
    private Long userId;
}
