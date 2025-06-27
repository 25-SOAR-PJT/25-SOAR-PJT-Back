package org.project.soar.model.tag;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import org.project.soar.model.field.Field;
import jakarta.persistence.Embeddable;

@Entity
@Table(name = "Tag")
@Data
public class Tag {
    @EmbeddedId
    private TagId id;

    @ManyToOne
    @MapsId("fieldId")
    @JoinColumn(name = "field_id")
    private Field field;
}

@Embeddable
@Data
class TagId implements Serializable {
    @Column(name = "tag_id")
    private String tagId;

    @Column(name = "field_id")
    private String fieldId;
}