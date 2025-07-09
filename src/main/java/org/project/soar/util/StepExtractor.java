package org.project.soar.util;

import org.project.soar.model.youthpolicy.YouthPolicyStep;

public class StepExtractor {

    public static YouthPolicyStep extractSteps(String policyId, String applyMethodContent,
            String submissionDocumentContent, String screeningMethodContent) {

        String fullContent = (applyMethodContent == null ? "" : applyMethodContent) +
                (screeningMethodContent == null ? "" : screeningMethodContent);

        String submittedDocuments = extractSubmittedDocuments(submissionDocumentContent);
        String step1 = containsAny(fullContent, "신청", "접수", "제출") ? "신청 접수" : null;
        String step2 = containsAny(fullContent, "서류", "필수서류", "제출서류", "PDF") ? "서류 검토" : null;
        String step3 = containsAny(fullContent, "심사", "선정방법", "우선순위") ? "심사 절차" : null;
        String step4 = containsAny(fullContent, "통보", "결과", "발표", "이메일", "문자") ? "발표 및 통보" : null;

        // 전처리 로그 확인
        System.out.println("step1: " + step1);
        System.out.println("step2: " + step2);
        System.out.println("step3: " + step3);
        System.out.println("step4: " + step4);
        System.out.println("submittedDocuments: " + submittedDocuments);

        // 모두 null이면 저장하지 않음
        if (submittedDocuments == null && step1 == null && step2 == null && step3 == null && step4 == null) {
            return null;
        }

        return YouthPolicyStep.builder()
                .policyId(policyId)
                .submittedDocuments(submittedDocuments)
                .step1(step1)
                .step2(step2)
                .step3(step3)
                .step4(step4)
                .build();
    }

    private static boolean containsAny(String content, String... keywords) {
        if (content == null)
            return false;
        for (String keyword : keywords) {
            if (content.contains(keyword))
                return true;
        }
        return false;
    }

    private static String extractSubmittedDocuments(String content) {
        return (content != null && !content.isBlank()) ? content.trim() : null;
    }
}
