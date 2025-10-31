package org.project.soar.model.alarm;

import lombok.RequiredArgsConstructor;
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

    public void createToken(User user, FcmTokenRegisterRequest dto) {

    }

    public void updateToken(User user, FcmTokenUpdateRequest dto) {
        Optional<FcmToken> existing = fcmTokenRepository.findByUserId(user.getUserId());
        FcmToken token = existing.get();
        token.setFcmToken(dto.getFcmToken());
        fcmTokenRepository.save(token);
    }

//TODO: 새로운 유저 토큰 저장
    public void registerToken(User user, FcmTokenRegisterRequest dto) {
        Optional<FcmToken> existing = fcmTokenRepository.findByUserId(user.getUserId());
        if (existing.isPresent()) {
            FcmToken token = existing.get();
            token.setFcmToken(dto.getFcmToken());
            token.setAlarmEnabledAll(dto.isAlarmEnabledAll());
            token.setAlarmEnabledScheduleRemind(dto.isAlarmEnabledScheduleRemind());
            token.setAlarmEnabledAppliedCheck(dto.isAlarmEnabledAppliedCheck());
            token.setAlarmEnabledReview(dto.isAlarmEnabledReview());
            fcmTokenRepository.save(token);
        } else{
            FcmToken token = new FcmToken(user.getUserId(), dto.getFcmToken(), true, false, false, false);

        }
    }


//    public void saveNewToken(User user, FcmTokenRegisterRequest dto) {
//        Optional<FcmToken> existing = fcmTokenRepository.findByUserId(user.getUserId());
//
//        if (existing.isPresent()) {
//            FcmToken token = existing.get();
//            token.setFcmToken(dto.getFcmToken());
//            fcmTokenRepository.save(token);
//        } else {
//
//        }
//    }

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



    //TODO: 이 아래 구현
//    public List<String> getAllTokensToPushByAlarmEnabled() {
//        Optional<List<FcmToken>> tokens = fcmTokenRepository.findAllByAlarmEnabledAllTrue();
//        if (tokens.isPresent()) {
//            return tokens.stream()
//                    .map(token -> token.getFcmToken())
//                    .collect(Collectors.toList());
//        }else{
//            return new ArrayList<>();
//        }
//    }

}
