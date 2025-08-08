package org.project.soar.model.tag.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.youthpolicytag.YouthPolicyTag;
import org.project.soar.model.tag.dto.ChatCompletion;
import org.project.soar.model.tag.service.ChatGPTService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatgpt")
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    /**
    chatGPT 모델리스트 조회
     **/
    @GetMapping("/modelList")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> selectModelList() {
        List<Map<String, Object>> result = chatGPTService.modelList();
        ApiResponse<List<Map<String, Object>>> response = ApiResponse.createSuccess(result);
        return ResponseEntity.ok(response);
    }
    /**
    chatGPT 유효한 모델인지 조회
     **/
    @GetMapping("/isValidModel")
    @Operation(summary = "chatGPT 유효한 모델인지 조회")
    public ResponseEntity<ApiResponse<Map<String, Object>>> isValidModel(@RequestParam String modelName) {
        Map<String, Object> result = chatGPTService.isValidModel(modelName);
        ApiResponse<Map<String, Object>> response = ApiResponse.createSuccess(result);
        return ResponseEntity.ok(response);
    }
    /**
    chatGPT 프롬프트 입력(일반 챗봇)
     **/
    @PostMapping("/prompt")
    @Operation(summary = "chatGPT 프롬프트 입력(일반 챗봇)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> selectPrompt(@RequestBody ChatCompletion chatCompletion) {
        Map<String, Object> result = chatGPTService.prompt(chatCompletion);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }
    /**
    chatGPT Prompt Management(프롬프트 튜닝 태깅)
     **/
    @PostMapping("/promptManagement")
    @Operation(summary = "youthPolicy_Tag 생성", description = "자동 태깅 실행")
    public ResponseEntity<ApiResponse<List<YouthPolicyTag>>> runPrompt() throws InterruptedException {
        List<YouthPolicyTag> result = chatGPTService.runPrompt();
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<List<YouthPolicyTag>>) ApiResponse.createError("empty"));
        }
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }
}

