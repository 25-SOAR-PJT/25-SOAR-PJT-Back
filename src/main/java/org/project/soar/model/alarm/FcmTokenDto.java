package org.project.soar.model.alarm;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class FcmTokenDto {
    private String token;
    private boolean alarmEnabledAll;
    private boolean alarmEnabledScheduleRemind;
    private boolean alarmEnabledAppliedCheck;
    private boolean alarmEnabledReview;
}
