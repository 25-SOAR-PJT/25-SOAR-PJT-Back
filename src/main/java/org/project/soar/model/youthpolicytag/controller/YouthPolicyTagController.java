package org.project.soar.model.youthpolicytag.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.tag.dto.YouthPolicyTagsResponse;
import org.project.soar.model.youthpolicytag.dto.FindYouthPolicyByTagResponse;
import org.project.soar.model.youthpolicytag.dto.YouthPolicyTagRequest;
import org.project.soar.model.youthpolicytag.dto.YouthPolicyTagResponse;
import org.project.soar.model.youthpolicytag.service.YouthPolicyTagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/youth-policy-tag")
public class YouthPolicyTagController {
    public final YouthPolicyTagService youthPolicyTagService;
    @GetMapping("/")
    @Operation(summary="전체 청년 정책 태그 조회", description="모든 청년 정책 태그를 조회합니다.")
    public ResponseEntity<ApiResponse<List<YouthPolicyTagResponse>>> getAllYouthPolicyTag() {
        List<YouthPolicyTagResponse> result = youthPolicyTagService.getAllYouthPolicyTag();
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @GetMapping("/tag")
    @Operation(summary = "하나의 태그로 정책 조회")
    public ResponseEntity<ApiResponse<FindYouthPolicyByTagResponse>> getYouthPolicyTagByTagId(@RequestParam("tagId") Long tagId) {
        FindYouthPolicyByTagResponse result = youthPolicyTagService.getYouthPolicyTagByTagId(tagId);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @GetMapping("/tags")
    @Operation(summary = "여러 개의 태그로 정책 조회")
    public ResponseEntity<ApiResponse<YouthPolicyTagsResponse>> getYouthPolicyTagByTagIds(@RequestParam("tagIds") List<Long> tagIds) {
        YouthPolicyTagsResponse result = youthPolicyTagService.getAllYouthPolicyByTagIds(tagIds);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @PostMapping("/")
    @Operation(summary = "수동 청년 정책 태그 생성")
    public ResponseEntity<ApiResponse<List<YouthPolicyTagResponse>>> createYouthPolicyTag(@RequestBody YouthPolicyTagRequest youthPolicyTagRequest) {
        List<YouthPolicyTagResponse> result = youthPolicyTagService.createYouthPolicyTag(youthPolicyTagRequest);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }
}
