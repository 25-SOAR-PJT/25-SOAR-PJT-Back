package org.project.soar.model.tag;

import lombok.Data;
import org.project.soar.model.youthpolicy.YouthPolicy;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.io.Serializable;

@Entity
@Table(name = "YouthPolicyTag")
@Data
public class YouthPolicyTag {
    @EmbeddedId
    private YouthPolicyTagId id;

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private YouthPolicy youthPolicy;
}

@Embeddable
@Data
class YouthPolicyTagId implements Serializable {
    private String projectId;

    @Column(name = "tag_id")
    private String tagId;

    @Column(name = "field_id")
    private String fieldId;
}
