package org.project.soar.model.tag.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.project.soar.model.tag.Tag;
import org.project.soar.model.youthpolicy.YouthPolicy;

import java.util.List;

@Data
@Getter
@Setter
public class YouthPolicyTagsResponse {
    private List<Tag> tags;
    private List<YouthPolicy> YouthPolicies;

    @Builder
    public YouthPolicyTagsResponse(List<Tag> tags, List<YouthPolicy> YouthPolicies) {
        this.tags = tags;
        this.YouthPolicies = YouthPolicies;
    }
}
