package org.project.soar.model.youthpolicy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YouthPolicyApiResponse {
    private int resultCode;
    private String resultMessage;
    private YouthPolicyApiResult result;
}
