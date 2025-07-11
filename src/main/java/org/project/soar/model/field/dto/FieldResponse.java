package org.project.soar.model.field.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class FieldResponse {
    private Long fieldId;
    private String fieldName;

    @Builder
    public FieldResponse(Long fieldId, String fieldName) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
    }
}