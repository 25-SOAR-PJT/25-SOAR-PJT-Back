package org.project.soar.model.user.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.soar.model.user.KakaoUser;
import org.project.soar.model.user.User;
import org.project.soar.model.user.enums.Role;
import org.project.soar.model.user.repository.KakaoUserRepository;
import org.project.soar.model.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

    @Value("${kakao.api_key}") // 카카오 API 키 (REST API 키)
    private String kakaoApiKey;

    @Value("${kakao.redirect_uri}") //카카오 로그인 Redirect URI
    private String kakaoRedirectUri;

    private final KakaoUserRepository kakaoUserRepository;
    private final UserRepository userRepository;

    /**
     * 카카오 API로부터 AccessToken을 받아오는 메서드
     * 
     * @param code 카카오에서 발급한 인증 코드
     * @return 카카오 AccessToken
     */
    public String getKakaoAccessToken(String code) {
        if (code == null || code.isEmpty()) {
            throw new RuntimeException("카카오 인증 코드가 제공되지 않았습니다.");
        }

        String accessToken = "";
        String requestURL = "https://kauth.kakao.com/oauth/token";

        try {
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            conn.setDoOutput(true);

            // 요청 파라미터 설정
            String params = "grant_type=authorization_code"
                    + "&client_id=" + kakaoApiKey
                    + "&redirect_uri=" + kakaoRedirectUri
                    + "&code=" + code;

            // 요청 전송
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()))) {
                writer.write(params);
                writer.flush();
            }

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            log.info("카카오 AccessToken 요청 응답 코드: " + responseCode);

            if (responseCode == 200) {
                // 응답 데이터 읽기
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    log.info("카카오 AccessToken 응답: " + result);

                    JsonElement element = JsonParser.parseString(result.toString());
                    accessToken = element.getAsJsonObject().get("access_token").getAsString();
                }
            } else {
                log.error("카카오 AccessToken 요청 실패: " + responseCode);
                throw new RuntimeException("카카오 인증 코드가 만료되었거나 유효하지 않습니다.");
            }

        } catch (Exception e) {
            log.error("카카오 AccessToken 요청 중 오류 발생", e);
        }

        return accessToken;
    }

    /**
     * 카카오 사용자 정보를 저장하거나 업데이트하는 메서드
     * 
     * @param accessToken 카카오에서 발급받은 AccessToken
     * @return KakaoUser 객체
     */
    public KakaoUser saveOrUpdateKakaoUser(String accessToken) {
        HashMap<String, Object> userInfo = getUserInfo(accessToken);

        if (userInfo.isEmpty()) {
            throw new RuntimeException("카카오에서 사용자 정보를 가져오지 못했습니다.");
        }

        String kakaoId = (String) userInfo.get("kakaoId");
        String nickname = (String) userInfo.get("nickname");

        Optional<KakaoUser> optionalKakaoUser = kakaoUserRepository.findByKakaoId(kakaoId);
        KakaoUser kakaoUser;

        if (optionalKakaoUser.isPresent()) {
            kakaoUser = optionalKakaoUser.get();
            log.info("기존 카카오 유저 로그인: " + kakaoUser.getKakaoNickname());
        } else {
            User newUser = User.builder()
                    .userName(nickname)
                    .userEmail(kakaoId + "@kakao.com") // 임시 이메일 설정
                    .userPassword("") // 소셜 로그인이라 비밀번호 설정 없음
                    .userGender(true) 
                    .userBirthDate(null)
                    .userRole(Role.USER)
                    .build();

            userRepository.save(newUser);

            // 카카오 유저 데이터 저장
            kakaoUser = KakaoUser.builder()
                    .kakaoId(kakaoId)
                    .kakaoNickname(nickname)
                    .user(newUser)
                    .accessToken(accessToken)
                    .build();

            log.info("새로운 카카오 유저 저장: " + kakaoUser.getKakaoNickname());
        }

        return kakaoUserRepository.save(kakaoUser);
    }

    /**
     * 카카오 API에서 사용자 정보를 가져오는 메서드
     * 
     * @param accessToken 카카오에서 발급한 AccessToken
     * @return 사용자 정보 (HashMap)
     */
    public HashMap<String, Object> getUserInfo(String accessToken) {
        HashMap<String, Object> userInfo = new HashMap<>();
        String requestURL = "https://kapi.kakao.com/v2/user/me";
        log.info("사용자 정보 요청용 accessToken = [{}]", accessToken);

        try {
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            int responseCode = conn.getResponseCode();
            log.info("카카오 사용자 정보 요청 응답 코드: " + responseCode);

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                log.info("카카오 사용자 정보 응답: " + result);

                JsonElement element = JsonParser.parseString(result.toString());
                JsonObject jsonObject = element.getAsJsonObject();

                // ID 가져오기 (필수)
                String kakaoId = jsonObject.get("id").getAsString();
                userInfo.put("kakaoId", kakaoId);

                // Properties가 존재하는지 확인 후 닉네임 가져오기
                if (jsonObject.has("properties") && !jsonObject.get("properties").isJsonNull()) {
                    JsonObject properties = jsonObject.getAsJsonObject("properties");
                    if (properties.has("nickname")) {
                        userInfo.put("nickname", properties.get("nickname").getAsString());
                    } else {
                        userInfo.put("nickname", "카카오유저");
                    }
                } else {
                    log.warn("카카오 사용자 정보에 properties 필드가 없음!");
                    userInfo.put("nickname", "카카오유저");
                }

            } else {
                log.error("카카오 사용자 정보 요청 실패: " + responseCode);
                throw new RuntimeException("카카오 사용자 정보를 가져오는 데 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("카카오 사용자 정보 요청 중 오류 발생", e);
        }

        return userInfo;
    }

    /**
     * 카카오 로그아웃 메서드
     * 
     * @param accessToken 카카오에서 발급받은 AccessToken
     */
    public void logout(String accessToken) {
        String requestURL = "https://kapi.kakao.com/v1/user/logout";

        try {
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            int responseCode = conn.getResponseCode();
            log.info("카카오 로그아웃 요청 응답 코드: " + responseCode);

            if (responseCode == 200) {
                log.info("카카오 로그아웃 성공");
            } else {
                log.error("카카오 로그아웃 실패: " + responseCode);
                throw new RuntimeException("카카오 로그아웃에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("카카오 로그아웃 중 오류 발생", e);
        }
    }
    /**
     * 카카오 계정 연결 해제 메서드
     * 
     * @param accessToken 카카오에서 발급받은 AccessToken
     */
    public void unlink(String accessToken) {
        String requestURL = "https://kapi.kakao.com/v1/user/unlink";

        try {
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            int responseCode = conn.getResponseCode();
            log.info("카카오 계정 연결 해제 요청 응답 코드: " + responseCode);

            if (responseCode == 200) {
                log.info("카카오 계정 연결 해제 성공");
            } else {
                log.error("카카오 계정 연결 해제 실패: " + responseCode);
                throw new RuntimeException("카카오 계정 연결 해제에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("카카오 계정 연결 해제 중 오류 발생", e);
        }
    }
    /**
     * 카카오 계정 연결 해제 후 사용자 정보 삭제 메서드
     * 
     * @param kakaoId 카카오 ID
     */
    public void deleteKakaoUser(String kakaoId) {
        Optional<KakaoUser> optionalKakaoUser = kakaoUserRepository.findByKakaoId(kakaoId);
        if (optionalKakaoUser.isPresent()) {
            KakaoUser kakaoUser = optionalKakaoUser.get();
            User user = kakaoUser.getUser();
            userRepository.delete(user);
            kakaoUserRepository.delete(kakaoUser);
            log.info("카카오 계정 연결 해제 및 사용자 정보 삭제 성공");
        } else {
            log.warn("카카오 계정이 존재하지 않습니다.");
        }
    }

    public String extractKakaoId(String accessToken) {
        HashMap<String, Object> userInfo = getUserInfo(accessToken);
        if (userInfo.containsKey("kakaoId")) {
            return (String) userInfo.get("kakaoId");
        } else {
            throw new RuntimeException("카카오 ID를 추출할 수 없습니다.");
        }
        
    }

}
