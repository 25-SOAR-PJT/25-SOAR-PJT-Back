package org.project.soar.model.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.project.soar.config.TokenProvider;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.category.dto.PopularPolicyDto;
import org.project.soar.model.category.service.CategoryService;
import org.project.soar.model.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;
    private final TokenProvider tokenProvider;

    // 1) 카테고리별 인기 지원사업 (코드)
    @GetMapping("/popular/by-code/{categoryCode}")
    @Operation(summary = "카테고리별 인기 지원사업(코드)", description = "categoryCode 기준으로 북마크 수가 많은 순으로 인기 정책을 조회합니다.")
    public ResponseEntity<?> getPopularByCode(
            HttpServletRequest request,
            @PathVariable int categoryCode,
            @RequestParam(defaultValue = "10") int size) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        List<PopularPolicyDto> result = categoryService.getPopularByCategoryCode(user, categoryCode, size);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    // 1-b) 카테고리별 인기 지원사업 (이름)
    @GetMapping("/popular/by-name/{categoryName}")
    @Operation(summary = "카테고리별 인기 지원사업(이름)", description = "카테고리명(일자리/주거/교육/복지문화) 기준으로 인기 정책을 조회합니다.")
    public ResponseEntity<?> getPopularByName(
            HttpServletRequest request,
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "10") int size) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        try {
            List<PopularPolicyDto> result = categoryService.getPopularByCategoryName(user, categoryName, size);
            return ResponseEntity.ok(ApiResponse.createSuccess(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.createError(e.getMessage()));
        }
    }

    // 2) 카테고리 인기 지원사업 + 태그 필터 (코드)
    @GetMapping("/popular/by-code/{categoryCode}/search-by-tags")
    @Operation(summary = "카테고리 인기 지원사업 태그 검색(코드)", description = "categoryCode 기준 인기 정책 중 tagIds에 해당하는 태그만 조회합니다.")
    public ResponseEntity<?> getPopularByCodeAndTags(
            HttpServletRequest request,
            @PathVariable int categoryCode,
            @RequestParam(name = "tagIds") List<Long> tagIds,
            @RequestParam(defaultValue = "10") int size) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        List<PopularPolicyDto> result = categoryService.getPopularByCategoryAndTags(user, categoryCode, tagIds, size);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    // 2-b) 카테고리 인기 지원사업 + 태그 필터 (이름)
    @GetMapping("/popular/by-name/{categoryName}/search-by-tags")
    @Operation(summary = "카테고리 인기 지원사업 태그 검색(이름)", description = "카테고리명 기준 인기 정책 중 지정한 tagIds와 매칭되는 정책만 조회합니다.")
    public ResponseEntity<?> getPopularByNameAndTags(
            HttpServletRequest request,
            @PathVariable String categoryName,
            @RequestParam(name = "tagIds") List<Long> tagIds,
            @RequestParam(defaultValue = "10") int size) {

        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        try {
            List<PopularPolicyDto> result = categoryService.getPopularByCategoryNameAndTags(user, categoryName, tagIds,
                    size);
            return ResponseEntity.ok(ApiResponse.createSuccess(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.createError(e.getMessage()));
        }
    }
    
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

}
