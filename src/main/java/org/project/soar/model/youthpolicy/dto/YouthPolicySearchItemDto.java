// org.project.soar.model.youthpolicy.dto.YouthPolicySearchItemDto
package org.project.soar.model.youthpolicy.dto;

import lombok.Builder;
import lombok.Getter;
import org.project.soar.model.youthpolicy.YouthPolicy;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class YouthPolicySearchItemDto {
    private String policyId;
    private String policyName;
    private String dateLabel;

    // 그대로 String(yyyyMMdd) 사용 — 엔티티 필드명과 동일
    private String businessPeriodEnd;

    // 보기 좋게 yyyy-MM-dd로 바꿔서 내려주고 싶으면 아래처럼 가공
    private String applicationEndDate; // yyyy-MM-dd or null

    public static YouthPolicySearchItemDto from(YouthPolicy p) {
        String appEnd = null;
        if (p.getApplicationEndDate() != null) {
            appEnd = p.getApplicationEndDate().toLocalDate()
                    .format(DateTimeFormatter.ISO_DATE); // yyyy-MM-dd
        }
        return YouthPolicySearchItemDto.builder()
                .policyId(p.getPolicyId())
                .policyName(p.getPolicyName())
                .dateLabel(p.getDateLabel())
                .businessPeriodEnd(p.getBusinessPeriodEnd()) // String 그대로
                .applicationEndDate(appEnd)
                .build();
    }
}
