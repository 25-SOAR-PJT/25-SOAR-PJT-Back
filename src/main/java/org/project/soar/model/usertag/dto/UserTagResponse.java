package org.project.soar.model.usertag.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.soar.model.tag.Tag;

import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class UserTagResponse {
    private Long userId;
    private List<Tag> tag;

    @Builder
    public UserTagResponse(Long userId, List<Tag> tag) {
        this.userId = userId;
        this.tag = tag;
    }
}
