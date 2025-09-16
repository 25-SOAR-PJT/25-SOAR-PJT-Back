package org.project.soar.model.alarm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FcmTokenUpdateRequest {
    @JsonProperty("fcm_token")
    private String fcmToken;
}
