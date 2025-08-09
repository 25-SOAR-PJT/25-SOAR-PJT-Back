package org.project.soar.model.youthpolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 달력에 표시할 "하루"의 카운트(신청 마감/사업 마감)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarDayResponseDto {
    private LocalDate date; // yyyy-MM-dd
    private int applyEndCount; // 신청 마감 개수
    private int businessEndCount; // 사업 마감 개수
    private List<PolicySummary> policies; // 정책 요약 리스트

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PolicySummary {
        private String policyId;
        private String policyName;
        private LocalDate deadline; // 마감일
    }
}
