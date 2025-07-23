package org.project.soar.model.youthpolicy;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "youth_policy")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicy {

    @Id
    @Column(name = "policy_id", length = 50)
    private String policyId;

    @Column(name = "policy_name", length = 500)
    private String policyName;

    @Column(name = "policy_keyword", length = 200)
    private String policyKeyword;

    @Column(name = "policy_explanation", columnDefinition = "TEXT")
    private String policyExplanation;

    @Column(name = "policy_support_content", columnDefinition = "TEXT")
    private String policySupportContent;

    @Column(name = "large_classification", length = 200)
    private String largeClassification;

    @Column(name = "medium_classification", length = 200)
    private String mediumClassification;

    @Column(name = "supervising_inst_code", length = 20)
    private String supervisingInstCode;

    @Column(name = "supervising_inst_name", length = 300)
    private String supervisingInstName;

    @Column(name = "operating_inst_code", length = 20)
    private String operatingInstCode;

    @Column(name = "operating_inst_name", length = 300)
    private String operatingInstName;

    @Column(name = "business_period_start", length = 8)
    private String businessPeriodStart;

    @Column(name = "business_period_end", length = 8)
    private String businessPeriodEnd;

    @Column(name = "business_period_etc", length = 500)
    private String businessPeriodEtc;

    @Column(name = "apply_method_content", columnDefinition = "TEXT")
    private String applyMethodContent;

    @Column(name = "screening_method_content", columnDefinition = "TEXT")
    private String screeningMethodContent;

    @Column(name = "apply_url", length = 1000)
    private String applyUrl;

    @Column(name = "submit_document_content", columnDefinition = "TEXT")
    private String submitDocumentContent;

    @Column(name = "etc_matter_content", columnDefinition = "TEXT")
    private String etcMatterContent;

    @Column(name = "reference_url1", length = 1000)
    private String referenceUrl1;

    @Column(name = "reference_url2", length = 1000)
    private String referenceUrl2;

    @Column(name = "support_scale_count", length = 20)
    private String supportScaleCount;

    @Column(name = "support_target_min_age")
    private Integer supportTargetMinAge;

    @Column(name = "support_target_max_age")
    private Integer supportTargetMaxAge;

    @Column(name = "support_target_age_limit_yn", length = 1)
    private String supportTargetAgeLimitYn;

    @Column(name = "earn_min_amt")
    private Long earnMinAmt;

    @Column(name = "earn_max_amt")
    private Long earnMaxAmt;

    @Column(name = "earn_etc_content", length = 500)
    private String earnEtcContent;

    @Column(name = "additional_apply_qualification", columnDefinition = "TEXT")
    private String additionalApplyQualification;

    @Column(name = "inquiry_count")
    private Integer inquiryCount;

    @Column(name = "zip_code", columnDefinition = "TEXT")
    private String zipCode;

    @Column(name = "policy_major_code", length = 100)
    private String policyMajorCode;

    @Column(name = "job_code", length = 100)
    private String jobCode;

    @Column(name = "school_code", length = 100)
    private String schoolCode;

    @Column(name = "first_reg_dt")
    private LocalDateTime firstRegDt;

    @Column(name = "last_modify_dt")
    private LocalDateTime lastModifyDt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "application_start_date")
    private LocalDateTime applicationStartDate;

    @Column(name = "application_end_date")
    private LocalDateTime applicationEndDate;

    @Column(name = "date_type", length = 20)
    private String dateType;

    @Column(name = "date_label", length = 50)
    private String dateLabel;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}