package org.project.soar.model.youthpolicytag;

import jakarta.persistence.*;
import lombok.*;
import org.project.soar.model.tag.Tag;
import org.project.soar.model.youthpolicy.YouthPolicy;

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
}
