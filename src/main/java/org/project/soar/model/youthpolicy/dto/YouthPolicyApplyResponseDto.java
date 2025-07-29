package org.project.soar.model.youthpolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class YouthPolicyApplyResponseDto {
    private String applyUrl;
    private String message;
    private String policyId;
    private Long userId;
    
}
