package org.project.soar.model.youthpolicy.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class YouthPolicyApplyToggleResponseDto {
    private String policyId;
    private boolean applied;   // 토글 후 상태: true=신청됨, false=신청취소됨
    private String applyUrl;   // 신청 시 URL이 있으면 포함
    private String message;    // 안내 메시지
}