package org.project.soar.model.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularPolicyDto {
    private String policyId;
    private String policyName;
    private Integer categoryCode;
    private Long bookmarkCount;
    private boolean isBookmarked;
}
