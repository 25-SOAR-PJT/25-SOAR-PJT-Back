package org.project.soar.model.tag.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class TagResponse {
    private Long tagId;
    private String tagName;
    private Long fieldId;

    @Builder
    public TagResponse(Long tagId, String tagName, Long fieldId) {
    this.tagId = tagId;
        this.tagName = tagName;
        this.fieldId = fieldId;
    }
}