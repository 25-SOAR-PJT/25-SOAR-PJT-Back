package org.project.soar.model.youthpolicy.dto;

import org.project.soar.util.DateClassifier;

/**
 * 정책 신청 가능 여부 및 처리 결과 상태
 */
public enum ApplyStatus {
    ELIGIBLE, // 신청 가능
    APPLIED, // 정상 신청 처리됨
    ALREADY_APPLIED, // 이미 신청함
    APPLY_ENDED, // 신청 마감
    BUSINESS_ENDED, // 사업 종료
    OPEN_UPCOMING, // 오픈 예정 (접수 시작 전)
    NOT_FOUND; // 정책 없음

    /**
     * DateClassifier 결과를 ApplyStatus로 변환
     * type: ONGOING, DEADLINE, FINISHED, UPCOMING
     * label: "신청 마감", "사업 종료", "오픈 예정 D-n" 등
     */
    public static ApplyStatus fromDateResult(DateClassifier.DateResult r) {
        if (r == null)
            return ELIGIBLE;

        String type = r.type();
        String label = r.label();

        if ("UPCOMING".equalsIgnoreCase(type)) {
            return OPEN_UPCOMING;
        }
        if ("FINISHED".equalsIgnoreCase(type)) {
            if (label != null && label.contains("신청 마감"))
                return APPLY_ENDED;
            return BUSINESS_ENDED;
        }
        return ELIGIBLE;
    }
}
