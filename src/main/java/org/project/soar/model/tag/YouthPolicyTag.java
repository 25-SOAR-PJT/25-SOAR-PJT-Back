package org.project.soar.model.tag;

import jakarta.persistence.*;
import lombok.*;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.field.Field;

@Entity
@Table(name = "youth_policy_tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicyTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", referencedColumnName = "policy_id", nullable = false)
    private YouthPolicy youthPolicy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @Builder
    public YouthPolicyTag(YouthPolicy youthPolicy, Tag tag, Field field) {
        this.youthPolicy = youthPolicy;
        this.tag = tag;
        this.field = field;
    }
}
