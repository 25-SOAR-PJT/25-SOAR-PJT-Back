package org.project.soar.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
public class DateClassifier {

    public static DateResult classify(
            LocalDate applyStart,
            LocalDate applyEnd,
            String businessPeriodEnd,
            String applyPeriodCode,
            String businessPeriodCode,
            String applyMethodContent,
            String screeningMethodContent) {

        LocalDate now = LocalDate.now();

        // ✅ 1. 코드상 상시로 명시된 경우
        if (("상시".equals(applyPeriodCode) || "99".equals(applyPeriodCode)) ||
                ("상시".equals(businessPeriodCode) || "99".equals(businessPeriodCode))) {
            return new DateResult("ONGOING", "상시");
        }

        // ✅ 2. 사업 종료일이 존재할 경우
        if (businessPeriodEnd != null && !businessPeriodEnd.isBlank()) {
            LocalDate endDate = parseDate(businessPeriodEnd);
            if (endDate != null) {
                long daysLeft = ChronoUnit.DAYS.between(now, endDate);
                if (daysLeft < 0) {
                    return new DateResult("FINISHED", "사업 종료");
                } else {
                    return new DateResult("CLOSING_SOON", "사업 종료 D-" + daysLeft);
                }
            }
        }

        // ✅ 3. 사업 종료일도 없고 신청 정보도 없는 경우
        if (applyStart == null && applyEnd == null &&
                isBlank(applyMethodContent) && isBlank(screeningMethodContent)) {
            return new DateResult("ONGOING", "상시");
        }

        // ✅ 4. 나머지 모든 경우
        return new DateResult("ONGOING", "상시");
    }

    public static LocalDate parseDate(String str) {
        try {
            if (str != null && !str.trim().isEmpty()) {
                return LocalDate.parse(str.trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", str);
        }
        return null;
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public record DateResult(String type, String label) {
    }
}
