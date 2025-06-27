package org.project.soar.model.youthpolicy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "YouthPolicyDetail")
@Data
public class YouthPolicyDetail {
    @Id
    @Column(name = "project_id")
    private String projectId;

    @Column(name = "info_detail")
    private String infoDetail;

    @Column(name = "target_detail")
    private String targetDetail;

    @Column(name = "support_detail")
    private String supportDetail;

    @OneToOne
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private YouthPolicy youthPolicy;
}
