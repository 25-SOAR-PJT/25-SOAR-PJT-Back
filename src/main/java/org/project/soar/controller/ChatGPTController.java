package org.project.soar.controller;

import lombok.extern.slf4j.Slf4j;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.tag.dto.ChatCompletionDto;
import org.project.soar.model.tag.service.ChatGPTService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chatgpt")
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    public ChatGPTController(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    // chatGPT 모델리스트 조회
    @GetMapping("/modelList")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> selectModelList() {
        List<Map<String, Object>> result = chatGPTService.modelList();
        ApiResponse<List<Map<String, Object>>> response = ApiResponse.createSuccess(result);
        return ResponseEntity.ok(response);
    }

    // chatGPT 유효한 모델인지 조회
    @GetMapping("/isValidModel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> isValidModel(@RequestParam String modelName) {
        Map<String, Object> result = chatGPTService.isValidModel(modelName);
        ApiResponse<Map<String, Object>> response = ApiResponse.createSuccess(result);
        return ResponseEntity.ok(response);
    }

    // chatGPT 프롬프트 입력(일반 챗봇)
    @PostMapping("/prompt")
    public ResponseEntity<Map<String, Object>> selectPrompt(@RequestBody ChatCompletionDto chatCompletionDto) {
        Map<String, Object> result = chatGPTService.prompt(chatCompletionDto);
        return ResponseEntity.ok(result);
    }

    // chatGPT Prompt Management(프롬프트 튜닝 태깅)
//    @PostMapping("/promptManagement")
//    public ResponseEntity<ApiResponse<String>> runPrompt() {
//        String result = chatGPTService.runPrompt();
//        return ResponseEntity.ok(ApiResponse.createSuccess(result));
//    }
}

