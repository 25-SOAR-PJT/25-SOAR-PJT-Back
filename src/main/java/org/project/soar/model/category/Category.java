package org.project.soar.model.category;

import lombok.Data;
import org.project.soar.model.youthpolicy.YouthPolicy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Category")
@Data
public class Category {
    @Id
    @Column(name = "category_id")
    private String categoryId;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private YouthPolicy youthPolicy;
}
