package org.project.soar.model.field.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.field.dto.FieldResponse;
import org.project.soar.model.field.service.FieldService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/field")
@RequiredArgsConstructor
public class FieldController {
    public final FieldService fieldService;

    /**
     * 디폴트 필드 리스트 넣기
     */
    @PostMapping("/")
    @Operation(summary = "디폴트 필드 리스트 넣기", description = "디폴트 필드 리스트를 넣습니다.")
    public ResponseEntity<ApiResponse<List<FieldResponse>>> setFieldList(){
        List<FieldResponse> result = fieldService.setFieldList();
        ApiResponse<List<FieldResponse>> response = ApiResponse.createSuccess(result);
        return ResponseEntity.ok(response);
    }

    /**
     * 필드 리스트 가져오기
     */
    @GetMapping("/")
    @Operation(summary = "필드 리스트 가져오기", description = "필드 리스트를 가져옵니다.")
    public ResponseEntity<ApiResponse<List<FieldResponse>>> getFieldList(){
        List<FieldResponse> result = fieldService.getFieldList();
        ApiResponse<List<FieldResponse>> response = ApiResponse.createSuccess(result);
        return ResponseEntity.ok(response);
    }
}
