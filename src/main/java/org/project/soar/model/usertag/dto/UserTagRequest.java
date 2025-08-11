package org.project.soar.model.usertag.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserTagRequest {
    private List<Long> tagId;

    @Builder
    public UserTagRequest(List<Long> tagId) {
        this.tagId = tagId;
    }
}
