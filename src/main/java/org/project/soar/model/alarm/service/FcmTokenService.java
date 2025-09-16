package org.project.soar.model.alarm.service;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.alarm.FcmToken;
import org.project.soar.model.alarm.dto.FcmTokenRegisterRequest;
import org.project.soar.model.alarm.repository.FcmTokenRepository;
import org.project.soar.model.user.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FcmTokenService {
    private final FcmTokenRepository fcmTokenRepository;

    // 유저의 토큰 새로 저장 + 업데이트
    public void createToken(User user, FcmTokenRegisterRequest dto) {
            Optional<FcmToken> fcmToken = fcmTokenRepository.findByUserId(user.getUserId());
            FcmToken newToken;
            if (fcmToken.isPresent()) {
                newToken = fcmToken.get();
                newToken.setFcmToken(dto.getFcmToken());
            } else {
                newToken = new FcmToken(user.getUserId(), dto.getFcmToken());
            }
            fcmTokenRepository.save(newToken);
        }

    public List<String> getAllTokensToPush() {
        if (fcmTokenRepository.findAll().isEmpty()) {
            return new ArrayList<>();
        }
        else {
            List<FcmToken> tokens = fcmTokenRepository.findAll();
            return tokens.stream()
                    .map(token -> token.getFcmToken())
                    .collect(Collectors.toList());
        }
    }

    public List<FcmToken> getAllToken() {
        return fcmTokenRepository.findAll();
    }

    public String getTokenByUserId(Long userId){
        if (fcmTokenRepository.findByUserId(userId).isPresent()) {
            return fcmTokenRepository.findByUserId(userId).get().getFcmToken();
        } else {
            return null;
        }
    }
}
