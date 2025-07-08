package org.project.soar.model.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.soar.model.auth.dto.EmailRequestDto;
import org.project.soar.model.auth.service.EmailService;
import org.project.soar.model.user.service.UserService;
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
    public ResponseEntity<String> sendOtp(@RequestBody @Valid EmailRequestDto emailRequestDto) {
        String email = emailRequestDto.getEmail().toLowerCase();

        emailService.setEmail(email);
        return ResponseEntity.ok("이메일로 인증 코드가 발송되었습니다.");
    }

    /**
     * 인증 코드 확인
     *
     * @param requestBody 이메일 + otp
     * @return 인증 결과 메시지
     */
    @PostMapping("/signup/otp/check")
    public String verifyOtp(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String otp = requestBody.get("otp");

        if (email == null || otp == null) {
            throw new IllegalArgumentException("이메일 또는 인증 코드가 누락되었습니다.");
        }

        if (emailService.checkAuthNumber(email, otp)) {
            return "이메일 인증 성공";
        }
        throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
    }
}
