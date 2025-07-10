package org.project.soar.model.tag;

import jakarta.persistence.*;
import lombok.*;
import org.project.soar.model.field.Field;

@Entity
@Table(name = "tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "tag_name", length = 100, nullable = false)
    private String tagName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    public Tag(String tag, Field field) {
        this.tagName = tag;
        this.field = field;
    }
}
