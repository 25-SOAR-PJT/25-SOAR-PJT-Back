package org.project.soar.model.youthpolicytag.controller;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Data
public class YouthPolicyTagRequest {
    private String youthPolicyId;
    private List<Long> tagIds;

    @Builder
public YouthPolicyTagRequest(String youthPolicyId, List<Long> tagIds) {
        this.youthPolicyId = youthPolicyId;
        this.tagIds = tagIds;
    }
}
