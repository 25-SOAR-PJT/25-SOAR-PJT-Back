package org.project.soar.model.youthpolicy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "YouthPolicyBanner")
@Data
public class YouthPolicyBanner {
    @Id
    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "img")
    private String img;

    @OneToOne
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private YouthPolicy youthPolicy;
}