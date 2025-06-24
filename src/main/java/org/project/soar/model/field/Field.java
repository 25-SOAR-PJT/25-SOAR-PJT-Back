package org.project.soar.model.field;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "Field")
@Data
public class Field {
    @Id
    @Column(name = "field_id")
    private Integer fieldId;

    @Column(name = "field_name")
    private Integer fieldName;
}
