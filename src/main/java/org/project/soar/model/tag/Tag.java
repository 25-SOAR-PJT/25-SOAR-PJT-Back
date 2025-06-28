package org.project.soar.model.tag;

import jakarta.persistence.*;
import lombok.*;
import org.project.soar.model.field.Field;

@Entity
@Table(name = "Tag")
@Data
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;

    @ManyToOne
    @JoinColumn(name = "field_id")
    private Field field;
}