package org.project.soar.model.alarm.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.config.TokenProvider;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.alarm.FcmToken;
import org.project.soar.model.alarm.dto.FcmTokenRegisterRequest;
import org.project.soar.model.alarm.service.FcmTokenService;
import org.project.soar.model.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmTokenController {
    private final FcmTokenService fcmTokenService;
    private final TokenProvider tokenProvider;
    @PostMapping("/apply/token")
    public ResponseEntity<?> registerToken(HttpServletRequest request, @RequestBody FcmTokenRegisterRequest dto) {
        log.info("FCM 토큰 등록 요청: {}", dto.getFcmToken());
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        fcmTokenService.createToken(user, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public void getAllToken() {
        List<FcmToken> fcmTokens = fcmTokenService.getAllToken();
        fcmTokens.stream().map(token -> token.getUserId().toString().concat(token.getFcmToken().toString())).forEach(System.out::println);
    }
}
