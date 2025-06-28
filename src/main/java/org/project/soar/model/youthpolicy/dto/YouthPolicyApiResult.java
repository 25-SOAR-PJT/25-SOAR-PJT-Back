package org.project.soar.model.youthpolicy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YouthPolicyApiResult {
    private YouthPolicyApiPaging pagging;
    private List<YouthPolicyApiData> youthPolicyList;
}
