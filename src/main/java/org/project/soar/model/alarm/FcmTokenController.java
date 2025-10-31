package org.project.soar.model.alarm;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.project.soar.config.TokenProvider;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmTokenController {
    private final FcmTokenService fcmTokenService;
    private final TokenProvider tokenProvider;
    @PostMapping("/apply/token/new")
    public ResponseEntity<?> registerToken(HttpServletRequest request, @RequestBody FcmTokenRegisterRequest dto) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        fcmTokenService.registerToken(user, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/apply/token")
    public ResponseEntity<?> updateToken(HttpServletRequest request, @RequestBody FcmTokenUpdateRequest dto) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        fcmTokenService.updateToken(user, dto);
        return ResponseEntity.ok().build();
    }
}
