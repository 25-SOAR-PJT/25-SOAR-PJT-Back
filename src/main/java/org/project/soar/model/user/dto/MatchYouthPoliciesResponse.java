package org.project.soar.model.user.dto;

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
public class MatchYouthPoliciesResponse {
    Long userId;
    List<Tag> tags;
    List<YouthPolicy> youthPolicyTags;

    @Builder
    public MatchYouthPoliciesResponse(Long userId, List<Tag> tags, List<YouthPolicy> youthPolicyTags) {
        this.userId = userId;
        this.tags = tags;
        this.youthPolicyTags = youthPolicyTags;
    }

}
