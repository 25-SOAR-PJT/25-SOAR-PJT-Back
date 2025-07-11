package org.project.soar.model.tag.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class YouthPolicyTagResponse {
    private String policyId;
    private Long tagId;
    @Builder
    public YouthPolicyTagResponse(String policyId, Long tagId){
        this.policyId = policyId;
        this.tagId = tagId;
    };
}
