package org.project.soar.model.youthpolicy.dto;
import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class YouthPolicyUserAgeItemDto {
    private String policyId;
    private String policyName;
    private String supervisingInstName;
    private String dateLabel;
    private boolean bookmarked;
    private String ageGroup;
}