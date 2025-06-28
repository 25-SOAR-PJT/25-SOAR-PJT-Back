package org.project.soar.model.youthpolicy;

import jakarta.persistence.*;
import lombok.*;
import org.project.soar.global.abstracts.BaseTimeEntity;
import java.time.LocalDateTime;

@Entity
@Table(name = "youth_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicy extends BaseTimeEntity {

    @Id
    @Column(name = "policy_id")
    private String policyId;

    @Column(name = "title", length = 2000)
    private String title;

    @Column(name = "project_name", length = 2000)
    private String projectName;

    @Column(name = "banner_img", length = 3000)
    private String bannerImg;

    @Column(name = "target_summary", length = 3000)
    private String targetSummary;

    @Column(name = "contact", length = 2000)
    private String contact;

    @Column(name = "application_start_date")
    private LocalDateTime applicationStartDate;

    @Column(name = "application_end_date")
    private LocalDateTime applicationEndDate;

    @Column(name = "support_date")
    private LocalDateTime supportDate;

    @Column(name = "selection_date")
    private LocalDateTime selectionDate;

    @Column(name = "objection_date")
    private LocalDateTime objectionDate;

    @Column(name = "final_result_date")
    private LocalDateTime finalResultDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "keywords", length = 2000)
    private String keywords;

    @Column(name = "support_content", columnDefinition = "TEXT")
    private String supportContent;

    @Column(name = "apply_url", length = 3000)
    private String applyUrl;

    @Column(name = "apply_period", length = 2000)
    private String applyPeriod;

    // API에서 추가로 받아올 수 있는 필드들 (실제 API 응답 기반)
    @Column(name = "supervising_institution", length = 2000)
    private String supervisingInstitution; // sprvsnInstCdNm

    @Column(name = "operating_institution", length = 2000)
    private String operatingInstitution; // operInstCdNm

    @Column(name = "min_age")
    private Integer minAge; // sprtTrgtMinAge

    @Column(name = "max_age")
    private Integer maxAge; // sprtTrgtMaxAge

    @Column(name = "support_scale", length = 2000)
    private String supportScale; // sprtSclCnt

    @Column(name = "large_classification", length = 1000)
    private String largeClassification; // lclsfNm

    @Column(name = "medium_classification", length = 1000)
    private String mediumClassification; // mclsfNm

    @Column(name = "business_period_etc", length = 2000)
    private String businessPeriodEtc; // bizPrdEtcCn

    @Column(name = "inquiry_count")
    private Integer inquiryCount; // inqCnt

    @Column(name = "reference_url1", length = 3000)
    private String referenceUrl1; // refUrlAddr1

    @Column(name = "reference_url2", length = 3000)
    private String referenceUrl2; // refUrlAddr2

    @Column(name = "apply_method_content", columnDefinition = "TEXT")
    private String applyMethodContent; // plcyAplyMthdCn

    @Column(name = "screening_method_content", columnDefinition = "TEXT")
    private String screeningMethodContent; // srngMthdCn

    @Column(name = "submit_document_content", columnDefinition = "TEXT")
    private String submitDocumentContent; // sbmsnDcmntCn

    @Column(name = "etc_matter_content", columnDefinition = "TEXT")
    private String etcMatterContent; // etcMttrCn
}
