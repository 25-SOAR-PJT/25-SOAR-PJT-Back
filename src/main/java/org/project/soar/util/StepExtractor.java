package org.project.soar.util;

import org.project.soar.model.youthpolicy.YouthPolicyStep;

import java.util.*;
import java.util.regex.Pattern;

public class StepExtractor {

    private static final Map<String, Pattern> STEP_PATTERNS = Map.of(
            "step1", Pattern.compile("(신청|접수|방문|온라인 신청|이메일)", Pattern.CASE_INSENSITIVE),
            "step2", Pattern.compile("(서류|자격|제출서류|검토)", Pattern.CASE_INSENSITIVE),
            "step3", Pattern.compile("(1차|2차|심사|위원회|고득점|면접|평가|신체검사|신용조회)", Pattern.CASE_INSENSITIVE),
            "step4", Pattern.compile("(발표|통보|연락|문자|홈페이지|게시)", Pattern.CASE_INSENSITIVE),
            "caution", Pattern.compile("(일정|날짜|조건|참고|공고문|계획|선발|포기|입금|계좌|교육)", Pattern.CASE_INSENSITIVE));

    public static YouthPolicyStep extractSteps(String policyId, String applyMethodContent,
            String submissionDocumentContent, String screeningMethodContent) {
        String raw = format(screeningMethodContent);
        List<String> fragments = splitRawText(raw);

        Map<String, List<String>> matchedSteps = new HashMap<>();
        for (String key : STEP_PATTERNS.keySet()) {
            matchedSteps.put(key, new ArrayList<>());
        }

        for (String frag : fragments) {
            boolean matched = false;
            for (Map.Entry<String, Pattern> entry : STEP_PATTERNS.entrySet()) {
                if (entry.getValue().matcher(frag).find()) {
                    matchedSteps.get(entry.getKey()).add(frag);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                matchedSteps.get("caution").add(frag); // 못 분류된 건 유의사항에
            }
        }

        return YouthPolicyStep.builder()
                .policyId(policyId)
                .submittedDocuments(format(submissionDocumentContent))
                .step1(joinOrDefault(matchedSteps.get("step1")))
                .step2(joinOrDefault(matchedSteps.get("step2")))
                .step3(joinOrDefault(matchedSteps.get("step3")))
                .step4(joinOrDefault(matchedSteps.get("step4")))
                .caution(joinOrDefault(matchedSteps.get("caution")))
                .build();
    }

    private static List<String> splitRawText(String text) {
        if (text == null || text.trim().isEmpty())
            return Collections.emptyList();
        return Arrays.stream(text.split("[▶→\\-–>\\n]+|●|·|\\*"))
                .map(String::trim)
                .filter(f -> !f.isEmpty())
                .toList();
    }

    private static String joinOrDefault(List<String> list) {
        return (list == null || list.isEmpty()) ? "없음" : String.join("<br>", list);
    }

    private static String format(String v) {
        if (v == null)
            return "없음";
        String trimmed = v.trim();
        return (trimmed.isEmpty() || trimmed.equals("-") || trimmed.equals("없음")) ? "없음" : trimmed;
    }
}
