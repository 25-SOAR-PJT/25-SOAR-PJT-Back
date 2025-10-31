package org.project.soar.model.alarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FcmTokenRegisterRequest {
    @JsonProperty("fcm_token")
    private String fcmToken;

    @JsonProperty("alarm_enabled_all")
    private boolean alarmEnabledAll;

    @JsonProperty("alarm_enabled_schedule_remind")
    private boolean alarmEnabledScheduleRemind;

    @JsonProperty("alarm_enabled_applied_check")
    private boolean alarmEnabledAppliedCheck;

    @JsonProperty("alarm_enabled_review")
    private boolean alarmEnabledReview;
}
