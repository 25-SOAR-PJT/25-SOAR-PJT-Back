package org.project.soar.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateClassifier {

    public static DateResult classify(
            LocalDate applyStart,
            LocalDate applyEnd,
            String businessPeriodEnd,
            String applyPeriodCode,
            String businessPeriodCode,
            String applyMethodContent,
            String screeningMethodContent,
            String businessPeriodEtc) {
        LocalDate now = LocalDate.now();

        // 1. 사업 종료일 우선 판별
        LocalDate bizEnd = parseDate(businessPeriodEnd);
        if (bizEnd != null && now.isAfter(bizEnd)) {
            return new DateResult("FINISHED", "사업 종료");
        }

        // 2. businessPeriodEtc에 과거 연도가 존재하면 종료 처리
        if (containsPastYearInEtc(businessPeriodEtc)) {
            return new DateResult("FINISHED", "사업 종료");
        }

        // 3. 마감일 존재 시 판별
        if (applyEnd != null) {
            long daysLeft = ChronoUnit.DAYS.between(now, applyEnd);
            if (daysLeft < 0) {
                return new DateResult("FINISHED", "신청 마감");
            } else if (daysLeft <= 3) {
                return new DateResult("DEADLINE", "신청 마감 D-" + daysLeft);
            } else {
                return new DateResult("ONGOING", "신청 가능");
            }
        }

        // 4. screeningMethodContent에 과거 날짜 명시
        if (containsPastDate(screeningMethodContent)) {
            return new DateResult("FINISHED", "사업 종료");
        }

        // 5. 예산 소진, 선착순 등
        if (applyMethodContent != null
                && (applyMethodContent.contains("예산 소진") || applyMethodContent.contains("선착순"))) {
            return new DateResult("ONGOING", "선착순 모집");
        }

        return new DateResult("ONGOING", "모집공고 확인");
    }

    private static LocalDate parseDate(String yyyymmdd) {
        try {
            if (yyyymmdd != null && !yyyymmdd.isBlank()) {
                return LocalDate.parse(yyyymmdd.trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
        } catch (Exception e) {
            // 무시
        }return null;

    }

    private static boolean containsPastDate(String content) {
        if (content == null || content.isBlank()) return false;

        Pattern datePattern = Pattern.compile("(20\\d{2})[.\\-/년 ]?(\\d{1,2})[.\\-/월 ]?(\\d{1,2})?");
        Matcher matcher = datePattern.matcher(content);

        while (matcher.find()) {
            try {
                String year = matcher.group(1);
                String month = matcher.group(2);
                String day = matcher.group(3) != null ? matcher.group(3) : "01";

                String normalized = String.format("%s%02d%02d", year, Integer.parseInt(month), Integer.parseInt(day));
                LocalDate foundDate = parseDate(normalized);
                if (foundDate != null && foundDate.isBefore(LocalDate.now())) {
                    return true;
                }
            } catch (Exception ignored) {}
        }

        return false;
    }

    private static boolean containsPastYearInEtc(String businessPeriodEtc) {
        if (businessPeriodEtc == null || businessPeriodEtc.isBlank()) return false;

        Pattern yearPattern = Pattern.compile("(20\\d{2})");
        Matcher matcher = yearPattern.matcher(businessPeriodEtc);
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        while (matcher.find()) {
            try {
                int year = Integer.parseInt(matcher.group(1));
                if (year < currentYear) {
                    return true;
                }
            } catch (Exception ignored) {}
        }

        return false;
    }

    public record DateResult(String type, String label) {
    }
}
