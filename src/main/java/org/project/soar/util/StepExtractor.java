package org.project.soar.util;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StepExtractor {

    private static final Pattern SPLIT_PATTERN = Pattern.compile("[\\-▶→●·\\*\\n]");
    private static final Map<String, Pattern> STEP_PATTERNS = Map.of(
        "apply", Pattern.compile("(신청|접수|지원|원서|모집)", Pattern.CASE_INSENSITIVE),
        "document", Pattern.compile("(서류|제출|자격|검토|자산심사|기록부|증빙|확인서)", Pattern.CASE_INSENSITIVE),
        "notice", Pattern.compile("(발표|통보|결과|안내|문자|홈페이지|게시|합격|이의|선정|면접)", Pattern.CASE_INSENSITIVE),
        "caution", Pattern.compile("(일정|날짜|참고|포기|계좌|교육|조건|공고문|주의|계획|기타|준수|보험)", Pattern.CASE_INSENSITIVE)
    );

    public static Map<String, List<String>> extractSteps(String... contents) {
        List<String> allSentences = Arrays.stream(contents)
            .filter(Objects::nonNull)
            .flatMap(content -> SPLIT_PATTERN.splitAsStream(content))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());

        List<String> applySteps = new ArrayList<>();
        List<String> documentSteps = new ArrayList<>();
        List<String> noticeSteps = new ArrayList<>();
        List<String> cautionSteps = new ArrayList<>();

        for (String sentence : allSentences) {
            if (STEP_PATTERNS.get("apply").matcher(sentence).find()) {
                applySteps.add(sentence);
            } else if (STEP_PATTERNS.get("document").matcher(sentence).find()) {
                documentSteps.add(sentence);
            } else if (STEP_PATTERNS.get("notice").matcher(sentence).find()) {
                noticeSteps.add(sentence);
            } else if (STEP_PATTERNS.get("caution").matcher(sentence).find()) {
                cautionSteps.add(sentence);
            } else {
                cautionSteps.add(sentence); // 미분류 문장도 caution으로
            }
        }

        return Map.of(
            "apply", defaultIfEmpty(applySteps),
            "document", defaultIfEmpty(documentSteps),
            "notice", defaultIfEmpty(noticeSteps),
            "caution", defaultIfEmpty(cautionSteps)
        );
    }

    private static List<String> defaultIfEmpty(List<String> list) {
        return list == null || list.isEmpty() ? List.of("없음") : list;
    }

    public static String joinHtmlList(List<String> steps) {
        if (steps == null || steps.isEmpty()) return "없음";
        return String.join("<br>", steps);
    }
}
