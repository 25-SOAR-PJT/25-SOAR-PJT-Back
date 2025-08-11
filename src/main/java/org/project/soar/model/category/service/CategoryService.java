package org.project.soar.model.category.service;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.category.CategoryType;
import org.project.soar.model.category.dto.PopularPolicyDto;
import org.project.soar.model.category.repository.CategoryRepository;
import org.project.soar.model.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /** 카테고리 이름 → 코드 변환 (일자리/주거/교육/복지문화) */
    private int toCategoryCode(String categoryName) {
        return CategoryType.fromName(categoryName)
                .map(CategoryType::getCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리명입니다: " + categoryName));
    }

    public List<PopularPolicyDto> getPopularByCategoryCode(User user, int categoryCode, int size) {
        return categoryRepository.findPopularPoliciesByCategory(
                categoryCode, user, PageRequest.of(0, Math.max(1, size)));
    }

    public List<PopularPolicyDto> getPopularByCategoryName(User user, String categoryName, int size) {
        int code = toCategoryCode(categoryName);
        return getPopularByCategoryCode(user, code, size);
    }

    public List<PopularPolicyDto> getPopularByCategoryAndTags(User user, int categoryCode, List<Long> tagIds,
            int size) {
        if (tagIds == null || tagIds.isEmpty()) {
            return getPopularByCategoryCode(user, categoryCode, size);
        }
        return categoryRepository.findPopularPoliciesByCategoryAndTagIds(
                categoryCode, tagIds, user, PageRequest.of(0, Math.max(1, size)));
    }

    public List<PopularPolicyDto> getPopularByCategoryNameAndTags(User user, String categoryName, List<Long> tagIds,
            int size) {
        int code = toCategoryCode(categoryName);
        return getPopularByCategoryAndTags(user, code, tagIds, size);
    }
}
