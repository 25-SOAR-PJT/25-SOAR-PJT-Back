package org.project.soar.model.youthpolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.util.DateClassifier;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class YouthPolicyBookmarkWithMetaResponseDto {
    private String policyId;
    private String policyName;
    private String dateLabel;          // 기존 라벨 로직 재사용
    private String businessPeriodEnd;  // 신청 마감일 이거를 추가
    private String dateType;           // ONGOING, DEADLINE, FINISHED, UPCOMING 이거를 추가
    private boolean applied;           // 신청 완료 여부
    private List<TagSimpleDto> tags;   // 정책 태그들

    public static YouthPolicyBookmarkWithMetaResponseDto from(YouthPolicy p,
                                                              boolean applied,
                                                              List<TagSimpleDto> tags) {

        // 기존과 동일한 방식으로 dateLabel 계산
        var r = DateClassifier.classify(
                p.getApplicationStartDate() == null ? null : p.getApplicationStartDate().toLocalDate(),
                p.getApplicationEndDate()   == null ? null : p.getApplicationEndDate().toLocalDate(),
                p.getBusinessPeriodEnd(),
                p.getPolicySupportContent(),
                p.getApplyMethodContent(),
                p.getScreeningMethodContent(),
                p.getBusinessPeriodEtc(),
                p.getPolicyName(),
                LocalDate.now()
        );

        return YouthPolicyBookmarkWithMetaResponseDto.builder()
                .policyId(p.getPolicyId())
                .policyName(p.getPolicyName())
                .dateLabel(r.label())
                .businessPeriodEnd(p.getBusinessPeriodEnd()) // ★ 추가됨
                .dateType(r.type())                          // ★ 추가됨
                .applied(applied)
                .tags(tags)
                .build();
    }
}