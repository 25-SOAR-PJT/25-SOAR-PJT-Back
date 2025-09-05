package org.project.soar.model.youthpolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TagSimpleDto {
    private Long tagId;
    private String tagName;
    private Integer fieldId;
}