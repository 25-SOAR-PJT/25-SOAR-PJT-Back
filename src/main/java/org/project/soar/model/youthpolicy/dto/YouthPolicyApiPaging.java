package org.project.soar.model.youthpolicy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YouthPolicyApiPaging {
    private int totCount;
    private int pageNum;
    private int pageSize;
}