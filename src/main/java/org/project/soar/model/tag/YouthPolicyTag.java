package org.project.soar.model.tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.project.soar.model.youthpolicy.YouthPolicy;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.io.Serializable;

@Entity
@Table(name = "youth_policy_tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class YouthPolicyTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private YouthPolicy youthPolicy;
}

@Embeddable
@Data
class YouthPolicyTagId implements Serializable {
    private Integer projectId;

    @Column(name = "tag_id")
    private Integer tagId;

    @Column(name = "field_id")
    private Integer fieldId;
}