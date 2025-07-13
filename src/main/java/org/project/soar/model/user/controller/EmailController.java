package org.project.soar.model.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.user.dto.EmailRequest;
import org.project.soar.model.user.service.EmailService;
import org.project.soar.model.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;
    private final UserService userService; // 추가

    /**
     * 이메일 인증 코드 발송
     *
     * @param emailRequestDto JSON 객체로 이메일 요청 데이터
     * @return 성공 메시지 또는 이미 구글 유저 에러
     */
    @PostMapping("/signup/otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@RequestBody @Valid EmailRequest emailRequestDto) {
        String email = emailRequestDto.getEmail().toLowerCase();

        String result = emailService.setEmail(email);

        // 이메일 형식 에러일 경우
        if (result.equals("이메일 형식을 다시 확인해주세요.")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<Void>) ApiResponse.createError(result));
        }else if (result.equals("이미 존재하는 이메일입니다.")){
            // 이미 존재하는 이메일일 경우
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<Void>) ApiResponse.createError(result));
        }

        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null, "이메일로 인증 코드가 발송되었습니다."));
    }

    /**
     * 인증 코드 확인
     *
     * @param requestBody 이메일 + otp
     * @return 인증 결과 메시지
     */
    @PostMapping("/signup/otp/check")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String otp = requestBody.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<Void>) ApiResponse.createError("이메일 또는 인증 코드가 누락되었습니다."));
        }

        if (emailService.checkAuthNumber(email, otp)) {
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null, "이메일 인증 성공"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<Void>) ApiResponse.createError("인증 코드가 일치하지 않습니다."));
    }
}
