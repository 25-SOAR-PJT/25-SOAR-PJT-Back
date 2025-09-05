package org.project.soar.model.youthpolicy.dto;

import lombok.Data;
import java.util.List;

@Data
public class YouthPolicyApiResult {
    private YouthPolicyApiPaging pagging;
    private List<YouthPolicyApiData> youthPolicyList;
}