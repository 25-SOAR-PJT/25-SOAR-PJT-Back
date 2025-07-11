package org.project.soar.model.tag.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.tag.dto.YouthPolicyTagResponse;
import org.project.soar.model.tag.service.YouthPolicyTagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/youthPolicyTag")
public class YouthPolicyTagController {
    public final YouthPolicyTagService youthPolicyTagService;
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<YouthPolicyTagResponse>>> getAllYouthPolicyTag() {
        ApiResponse<List<YouthPolicyTagResponse>> response = ApiResponse.createSuccess(youthPolicyTagService.getAllYouthPolicyTag());
        return ResponseEntity.ok(response);
    }
}
