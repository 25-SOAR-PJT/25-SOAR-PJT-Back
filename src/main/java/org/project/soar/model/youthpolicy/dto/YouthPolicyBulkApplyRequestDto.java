package org.project.soar.model.youthpolicy.dto;

import lombok.Data;

import java.util.List;

/**
 * 배치 신청 요청 DTO
 */
@Data
public class YouthPolicyBulkApplyRequestDto {
    private List<String> policyIds;
}
