package org.project.soar.model.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.user.dto.*;
import org.project.soar.model.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Validated @RequestBody SignUpRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<SignUpResponse>) ApiResponse.createFail(bindingResult));
        }

        SignUpResponse response = userService.signUp(request, request.getOtp(), request.getAgreedTerms());
        if (response.getMsg().equals("회원가입 성공")) {
            return ResponseEntity.ok(ApiResponse.createSuccess(response));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<SignUpResponse>) ApiResponse.createError(response.getMsg()));
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<SignInResponse>> signIn(@Validated @RequestBody SignInRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<SignInResponse>) ApiResponse.createFail(bindingResult));
        }

        SignInResponse response = userService.signIn(request);
        if (response.getMsg().equals("로그인 성공")) {
            return ResponseEntity.ok(ApiResponse.createSuccess(response));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<SignInResponse>) ApiResponse.createError(response.getMsg()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken (
            @RequestHeader("Refresh-Token") String refreshToken,
            @RequestHeader(value = "Authorization", required = false) String oldAccessBearer)
            throws JsonProcessingException {
        logger.info("[RefreshController] Refresh-Token 요청: {}", refreshToken);
        logger.info("                 oldAccessBearer: {}", oldAccessBearer);
        TokenResponse body = userService.refreshToken(refreshToken, oldAccessBearer);
        logger.info("🔄 [RefreshController] 응답으로 반환할 새 토큰들: {}", body);
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(body, "토큰 갱신 성공"));
    }

    @PostMapping("/signout")
    public ResponseEntity<ApiResponse<String>> signOut(@RequestBody Map<String, String> request)
            throws JsonProcessingException {
        String token = request.get("token");
        String response = userService.signOut(token);
        if (response.equals("로그아웃 성공")) {
            return ResponseEntity.ok(ApiResponse.createSuccess(response));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<String>) ApiResponse.createError(response));
    }
}
