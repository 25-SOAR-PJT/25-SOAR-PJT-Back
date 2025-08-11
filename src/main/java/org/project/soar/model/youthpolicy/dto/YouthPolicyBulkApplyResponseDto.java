package org.project.soar.model.youthpolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 배치 신청 응답 요약 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicyBulkApplyResponseDto {
    private Long userId;
    private int requestedCount;
    private int appliedCount;
    private int alreadyAppliedCount;
    private int applyEndedCount;
    private int businessEndedCount;
    private int openUpcomingCount; // 오픈 예정
    private int notFoundCount;
    private List<YouthPolicyBulkApplyItemResultDto> results;
}
