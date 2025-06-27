package org.project.soar.model.youthpolicy;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "YouthPolicy")
@Data
public class YouthPolicy {
    @Id
    @Column(name = "policy_id")
    private String policyId;

    private String title;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "banner_img")
    private String bannerImg;

    @Column(name = "target_summary")
    private String targetSummary;

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

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "description")
    private String description;

    @Column(name = "keywords")
    private String keywords;

    @Column(name = "support_content")
    private String supportContent;

    @Column(name = "apply_url")
    private String applyUrl;

    @Column(name = "apply_period")
    private String applyPeriod;
}
