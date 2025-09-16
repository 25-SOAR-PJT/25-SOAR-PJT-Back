package org.project.soar.model.youthpolicy.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class PolicyIdListRequestDto {
    private List<String> policyIds;
}
