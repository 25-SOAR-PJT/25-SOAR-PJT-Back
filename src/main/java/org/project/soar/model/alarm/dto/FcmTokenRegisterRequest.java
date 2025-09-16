package org.project.soar.model.alarm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FcmTokenRegisterRequest {
    @JsonProperty("fcmToken")
    private String fcmToken;
}
