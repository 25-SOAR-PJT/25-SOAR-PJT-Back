package org.project.soar.model.alarm.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.config.TokenProvider;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.alarm.service.FcmTokenService;
import org.project.soar.model.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.project.soar.util.FCMSender;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class AlarmController {
    private final FCMSender fcm;
    private final FcmTokenService fcmTokenService;
    private final TokenProvider tokenProvider;
    @PostMapping("/single")
    public ResponseEntity<?> push(@RequestBody PushReq req) {
        try {
            String id = fcm.sendToToken(req.token(), req.title(), req.body(), req.data());
            return ResponseEntity.ok(Map.of("messageId", id));
        } catch (FirebaseMessagingException e) {
            // 토큰 만료/등록 해제 등 코드 핸들링>
            return ResponseEntity.status(502).body(Map.of("error", e.getErrorCode(), "msg", e.getMessage()));
        }
    }
    @PostMapping("/topic")
    public ResponseEntity<?> pushToTopic(@RequestBody PushTopicReq req) {
        try {
            String id = fcm.sendToTopic(req.topic(), req.title(), req.body(), req.data());
            return ResponseEntity.ok(Map.of("messageId", id));
        } catch (FirebaseMessagingException e) {
            return ResponseEntity.status(502).body(Map.of("error", e.getErrorCode(), "msg", e.getMessage()));
        }
    }

    @PostMapping("/all")
    public ResponseEntity<?> pushToMulticast(@RequestBody PushAllReq req) {
        List<String> tokens = fcmTokenService.getAllTokensToPush();
        for (String token : tokens) {
            try {
                String messageId = fcm.sendToToken(token, req.title(), req.body(), req.data());
                log.info("Push sent to token: {}", token);
            } catch (FirebaseMessagingException e) {
                log.warn("Failed to send to token {}: {}", token, e.getMessage());
            }
        }
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/attendance_check")
    public ResponseEntity<?> attendance_check(HttpServletRequest request) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        try {
            String id = fcm.sendToToken(fcmTokenService.getTokenByUserId(user.getUserId()), "SOAR", "신청이 완료 됐나요? 앱으로 돌아와서 완료표시를 해보세요!", null);
            return ResponseEntity.ok(Map.of("messageId", id));
        } catch (FirebaseMessagingException e) {
            // 토큰 만료/등록 해제 등 코드 핸들링>
            return ResponseEntity.status(502).body(Map.of("error", e.getErrorCode(), "msg", e.getMessage()));
        }
    }



    public record PushTopicReq(String topic, String title, String body, Map<String, String> data) {}
    public record PushReq(String token, String title, String body, Map<String,String> data) {}

    public record PushAllReq(String title, String body, Map<String,String> data) {}

}