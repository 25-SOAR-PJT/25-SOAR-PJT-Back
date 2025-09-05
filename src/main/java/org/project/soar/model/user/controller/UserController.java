package org.project.soar.model.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.config.TokenProvider;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.user.User;
import org.project.soar.model.user.dto.*;
import org.project.soar.model.user.service.UserService;
import org.project.soar.model.usertag.service.UserTagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    private final UserTagService userTagService;
    private final TokenProvider tokenProvider;
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
        logger.info("[RefreshController] 응답으로 반환할 새 토큰들: {}", body);
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(body, "토큰 갱신 성공"));
    }

    @PostMapping("/signout")
    public ResponseEntity<ApiResponse<String>> signOut(@RequestHeader("Authorization") String token)
            throws JsonProcessingException {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        log.info("token: " + token);
        String response = userService.signOut(token);
        if (response.equals("로그아웃 성공")) {
            return ResponseEntity.ok(ApiResponse.createSuccess(response));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<String>) ApiResponse.createError(response));
    }

    //카카오 로그인
    @PostMapping("/kakao/signin")
    public ResponseEntity<ApiResponse<KakaoLoginResponse>> kakaoSignIn(@RequestBody KakaoLoginRequest request) {
        log.info("Raw Request Body: " + request);
        log.info("Kakao SignIn endpoint hit with token: " + request.getAccessToken());

        if (request.getAccessToken() == null || request.getAccessToken().isEmpty()) {
            log.error("카카오 로그인 실패: 토큰이 없습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<KakaoLoginResponse>) ApiResponse.createError("카카오 액세스 토큰이 없습니다."));
        }

        KakaoLoginResponse response = userService.kakaoSignIn(request.getAccessToken());

        if ("카카오 로그인 성공".equals(response.getMsg())) {
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(response, "카카오 로그인 성공"));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<KakaoLoginResponse>) ApiResponse.createError(response.getMsg()));
    }

    // 이미 존재하는 이메일
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = userService.checkEmailExists(email);
        if (exists) {
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(true, "이미 존재하는 이메일입니다."));
        }
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(false, "사용 가능한 이메일입니다."));
    }

    // 아이디 찾기 (이름, 생년월일)
    @GetMapping("/find-id")
    public ResponseEntity<ApiResponse<FindIdResponse>> findId(
            @RequestParam String userName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate userBirthDate,
            @RequestParam boolean userGender) {

        FindIdResponse response = userService.findId(userName,userBirthDate, userGender);

        if (response != null) {
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(response, "아이디 찾기 성공"));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body((ApiResponse<FindIdResponse>) ApiResponse.createError("아이디 찾기 실패"));
    }

    // 비밀번호 찾기 (임시 비밀번호 발급)
    @PostMapping("/find-password")
    public ResponseEntity<ApiResponse<String>> findPassword(@RequestBody FindPasswordRequest request) {
        String result = userService.findPassword(request.getUserEmail(), request.getUserName());
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null,result));
    }

    // 비밀번호 재설정 (이메일, 현재 비밀번호, 새 비밀번호, 비밀번호 확인)
    @PostMapping("/update-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestBody UpdatePasswordRequest request) {
        String userEmail = request.getUserEmail();
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (userEmail == null || currentPassword == null || newPassword == null || confirmPassword == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("필수 입력값이 누락되었습니다."));
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("새 비밀번호와 비밀번호 확인이 일치하지 않습니다."));
        }

        String result = userService.updatePassword(userEmail, currentPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null, result));
    }

    @PostMapping("/update-name")
    public ResponseEntity<ApiResponse<String>> updateUserName(@RequestBody UpdateUserNameRequest request) {
        Long userId = request.getUserId();
        String newUserName = request.getUserName();

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((ApiResponse<String>) ApiResponse.createError("사용자 ID를 입력해주세요."));
        }

        if (newUserName == null || newUserName.equals("")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((ApiResponse<String>) ApiResponse.createError("새로운 이름을 입력해주세요."));
        }

        String result = userService.updateUserName(userId, newUserName);
        if (result.equals("사용자 이름 업데이트 성공")) {
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null, result));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<String>) ApiResponse.createError(result));
    }

    @GetMapping("/get-userinfo")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(HttpServletRequest request) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body((ApiResponse<UserInfoResponse>) ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        String userAddress = userTagService.getUserResidence(user);
        UserInfoResponse userInfo = userService.getUserInfo(user,userAddress);
        if (userInfo != null) {
            return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(userInfo, "사용자 정보 조회 성공"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body((ApiResponse<UserInfoResponse>) ApiResponse.createError("사용자를 찾을 수 없습니다."));
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<?>> deleteUser(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String password = request.get("password");
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createError("비밀번호가 필요합니다."));
        }

        try {
            String result = userService.deleteUser(token, password);
            if (result.equals("회원 탈퇴 성공")) {
                return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null, result));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.createError(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.createError("회원 탈퇴 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/kakao/delete")
    public ResponseEntity<ApiResponse<?>> deleteKakaoUser(@RequestHeader("Authorization") String token) {
        log.info(">> /kakao/withdrawal 진입");
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            log.info(">> 토큰 파싱 후: {}", token);
            String result = userService.deleteKakaoUser(token);
            log.info(">> 탈퇴 처리 결과: {}", result);
            if (result.equals("카카오 사용자 삭제 성공")) {
                return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(null, result));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.createError(result));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.createError("카카오 회원 탈퇴 실패: " + e.getMessage()));
        }
    }
    @PostMapping("/match-policies")
    public ResponseEntity<ApiResponse<MatchYouthPoliciesResponse>> findMatchPolicies(@RequestParam("userId") Long userId) {
        MatchYouthPoliciesResponse result = userService.getMatchPolicies(userId);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }
}
