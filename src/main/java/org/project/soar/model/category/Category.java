package org.project.soar.model.category;

import jakarta.persistence.*;
import lombok.*;
import org.project.soar.model.youthpolicy.YouthPolicy;

@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_code", nullable = false)
    private int categoryCode;

    @Transient 
    private String categoryName;

    @PostLoad
    public void loadName() {
        this.categoryName = CategoryType.fromCode(categoryCode)
                .map(CategoryType::getName)
                .orElse("기타");
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", referencedColumnName = "policy_id", nullable = false)
    private YouthPolicy youthPolicy;
}
