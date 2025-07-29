package org.project.soar.model.category.repository;

import org.project.soar.model.category.Category;
import java.util.Optional;

import org.project.soar.model.youthpolicy.YouthPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryCodeAndYouthPolicy(int categoryCode, YouthPolicy policy);
}
