package org.project.soar.model.field;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "field")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "field_id")
    private Long fieldId;

    @Column(name = "field_name", length = 100, nullable = false)
    private String fieldName;

    public Field(String name) {
        this.fieldName = name;
    }
}