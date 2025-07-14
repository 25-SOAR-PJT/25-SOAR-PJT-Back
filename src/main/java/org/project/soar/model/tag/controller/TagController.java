package org.project.soar.model.tag.controller;

import lombok.RequiredArgsConstructor;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.tag.dto.TagResponse;
import org.project.soar.model.tag.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;
    /**
     * 태그 리스트 조회
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTagList(){
        List<TagResponse> result = tagService.getAllTagList();
        ApiResponse<List<TagResponse>> response = ApiResponse.createSuccess(result);
        return ResponseEntity.ok(response);
    }

    /**
     기본 태그 리스트 생성
     **/
    @PostMapping("/")
    public ResponseEntity<ApiResponse<List<TagResponse>>> setTagList(){
        List<TagResponse> result = tagService.setTagList();
        ApiResponse<List<TagResponse>> response = ApiResponse.createSuccess(result);
        return ResponseEntity.ok(response);
    }
}
