package org.project.soar.model.youthpolicytag.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class FindYouthPolicyByTagResponse {
    private Long tagId;
    private String tagName;
    private List<String> policyIds;
    @Builder
    public FindYouthPolicyByTagResponse(Long tagId, String tagName, List<String> policyIds){
        this.tagId = tagId;
        this.tagName = tagName;
        this.policyIds = policyIds;
    }
}
