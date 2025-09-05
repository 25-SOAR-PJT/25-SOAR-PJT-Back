package org.project.soar.model.youthpolicy.dto;
import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class YouthPolicyMainItemDto {
    private String policyId;
    private String policyName;
    private String policyKeyword;
    private String largeClassification;
    private String mediumClassification;
    private String supervisingInstName;
    private String dateLabel;
    private boolean bookmarked;
}