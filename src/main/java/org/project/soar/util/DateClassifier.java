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
            String applyMethodContent,
            String screeningMethodContent,
            String businessPeriodEtc,
            String policySupportContent,
            String policyName,
            LocalDate currentDate) {

        if (currentDate == null)
            currentDate = LocalDate.now();

        // 1. 정책명에 과거 연도 포함
        if (containsPastYearInText(policyName, currentDate)) {
            return new DateResult("FINISHED", "사업 종료");
        }

        // 2. 신청 마감일 판단 (우선순위 상향)
        if (applyEnd != null) {
            long daysLeftApply = ChronoUnit.DAYS.between(currentDate, applyEnd);
            if (daysLeftApply < 0) {
                return new DateResult("FINISHED", "신청 마감");
            } else if (daysLeftApply <= 3) {
                return new DateResult("DEADLINE", "신청 마감 D-" + daysLeftApply);
            }
        }

        // 3. 사업 마감일 판단 (항상 D-n 출력)
        LocalDate bizEnd = parseDate(businessPeriodEnd);
        if (bizEnd != null) {
            long daysLeftBiz = ChronoUnit.DAYS.between(currentDate, bizEnd);
            if (daysLeftBiz < 0) {
                return new DateResult("FINISHED", "사업 종료");
            } else {
                return new DateResult("DEADLINE", "사업 마감 D-" + daysLeftBiz);
            }
        }

        // 4. 텍스트 기반 과거 정보 포함 시 종료 처리
        if (containsPastYearInText(businessPeriodEtc, currentDate)
                || containsPastYearInText(policySupportContent, currentDate)
                || containsEndKeyword(businessPeriodEtc)
                || containsEndKeyword(policySupportContent)
                || containsPastDate(policySupportContent, currentDate)) {
            return new DateResult("FINISHED", "사업 종료");
        }

        // 5. 심사 방식 설명에 과거 날짜 or 종료 키워드 포함
        if (containsPastDate(screeningMethodContent, currentDate) ||
                containsEndKeyword(screeningMethodContent)) {
            return new DateResult("FINISHED", "사업 종료");
        }

        // 6. 신청 방식 설명에 선착순 / 예산 소진 키워드 포함
        if (applyMethodContent != null &&
                (applyMethodContent.contains("예산 소진") || applyMethodContent.contains("선착순"))) {
            return new DateResult("ONGOING", "선착순 모집");
        }

        // 7. 기본값
        return new DateResult("ONGOING", "모집공고 확인");
    }

    private static LocalDate parseDate(String yyyymmdd) {
        try {
            if (yyyymmdd != null && !yyyymmdd.isBlank()) {
                return LocalDate.parse(yyyymmdd.trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean containsPastYearInText(String text, LocalDate currentDate) {
        if (text == null || text.isBlank())
            return false;

        Pattern yearPattern = Pattern.compile("(20\\d{2})");
        Matcher matcher = yearPattern.matcher(text);
        int currentYear = currentDate.getYear();

        while (matcher.find()) {
            try {
                int year = Integer.parseInt(matcher.group(1));
                if (text.contains("~")) {
                    String[] split = text.split("~");
                    if (split.length == 2) {
                        int endYear = Integer.parseInt(split[1].replaceAll("[^0-9]", ""));
                        if (endYear < currentYear)
                            return true;
                    }
                } else if (year < currentYear) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private static boolean containsEndKeyword(String content) {
        if (content == null)
            return false;
        return content.contains("지원 마감") || content.contains("사업 종료")
                || content.contains("접수 마감") || content.contains("모집 마감")
                || content.contains("종료") || content.contains("발표 완료");
    }

    private static boolean containsPastDate(String content, LocalDate currentDate) {
        if (content == null || content.isBlank())
            return false;

        Pattern datePattern = Pattern.compile("(20\\d{2})[.\\-/년 ]?(\\d{1,2})[.\\-/월 ]?(\\d{1,2})?");
        Matcher matcher = datePattern.matcher(content);

        while (matcher.find()) {
            try {
                String year = matcher.group(1);
                String month = matcher.group(2);
                String day = matcher.group(3) != null ? matcher.group(3) : "01";

                String normalized = String.format("%s%02d%02d", year, Integer.parseInt(month), Integer.parseInt(day));
                LocalDate foundDate = parseDate(normalized);
                if (foundDate != null && foundDate.isBefore(currentDate)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public record DateResult(String type, String label) {
    }
}
