package org.project.soar.model.usertag.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserTagRequest {
    private Long userId;
    private List<Long> tagId;

    @Builder
    public UserTagRequest(Long userId, List<Long> tagId) {
        this.userId = userId;
        this.tagId = tagId;
    }
}
