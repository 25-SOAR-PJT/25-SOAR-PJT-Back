package org.project.soar.model.tag.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class YouthPolicyTagResponse {
    private String policyId;
    private String policyName;
    private Long tagId;
    private String tagName;
    @Builder
    public YouthPolicyTagResponse(String policyId, String policyName, Long tagId, String tagName){
        this.policyId = policyId;
        this.policyName = policyName;
        this.tagId = tagId;
        this.tagName = tagName;
    }
}
