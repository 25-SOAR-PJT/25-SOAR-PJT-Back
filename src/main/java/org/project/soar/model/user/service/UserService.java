package org.project.soar.model.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.config.TokenProvider;
import org.project.soar.model.permission.service.PermissionService;
import org.project.soar.model.user.KakaoUser;
import org.project.soar.model.user.RefreshToken;
import org.project.soar.model.user.User;
import org.project.soar.model.user.dto.*;
import org.project.soar.model.user.repository.KakaoUserRepository;
import org.project.soar.model.user.repository.RefreshTokenRepository;
import org.project.soar.model.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final EmailService emailService;
    private final PermissionService permissionService;
    private final KakaoService kakaoService;
    private final KakaoUserRepository kakaoUserRepository;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request, String otp, List<Boolean> agreedTerms) {
        String normalizedEmail = request.getUserEmail().trim().toLowerCase(Locale.getDefault());

        if (userRepository.existsByUserEmail(request.getUserEmail())) {
            return SignUpResponse.builder()
                    .msg("이메일이 이미 존재합니다.")
                    .build();
        }
        // 이메일 인증 코드 확인
        if (!emailService.checkAuthNumber(normalizedEmail, otp)) {
            return SignUpResponse.builder()
                    .msg("이메일 인증 코드가 유효하지 않습니다.")
                    .build();
        }

        // 비밀번호 보안 규칙 확인
        if (!isValidPassword(request.getUserPassword())) {
            return SignUpResponse.builder()
                    .msg("비밀번호 형식이 올바르지 않습니다. 비밀번호는 8자 이상 16자 이하, 문자, 숫자, 특수문자를 포함해야 합니다.")
                    .build();
        }

        User user = User.builder()
                .userName(request.getUserName())
                .userBirthDate(request.getUserBirthDate().toLocalDate())
                .userPhoneNumber(request.getUserPhoneNumber())
                .userGender(request.isUserGender())
                .userEmail(request.getUserEmail())
                .userPassword(passwordEncoder.encode(request.getUserPassword()))
                .build();

        userRepository.save(user);

        // 필수 약관 동의 확인
        if (!permissionService.hasAgreedToRequiredTerms(user)) {
            return SignUpResponse.builder()
                    .msg("필수 약관에 동의해야 회원가입이 가능합니다.")
                    .build();
        }
        // 약관 동의 저장
        permissionService.saveAgreedTerms(user, agreedTerms);

        return SignUpResponse.builder()
                .msg("회원가입 성공")
                .build();
    }

    @Transactional
    public SignInResponse signIn(SignInRequest request) {
        if (request == null || request.getUserEmail() == null || request.getUserPassword() == null) {
            return SignInResponse.builder()
                    .msg("이메일 또는 비밀번호가 제공되지 않았습니다.")
                    .build();
        }

        String normalizedEmail = request.getUserEmail().trim().toLowerCase(Locale.getDefault());
        User user = userRepository.findByUserEmail(normalizedEmail);
        if (user == null) {
            return SignInResponse.builder()
                    .msg("이메일이 존재하지 않습니다.")
                    .build();
        }

        if (!passwordEncoder.matches(request.getUserPassword(), user.getUserPassword())) {
            return SignInResponse.builder()
                    .msg("비밀번호가 일치하지 않습니다.")
                    .build();
        }

        // User 객체를 기반으로 토큰을 생성합니다.
        String accessToken = tokenProvider.createToken(user);
        String refreshToken = tokenProvider.createRefreshToken(user);

        Optional<RefreshToken> oldRefreshToken = refreshTokenRepository.findById(user.getUserId());
        if (oldRefreshToken.isEmpty()) {
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .tokenId(user.getUserId())
                    .refreshToken(refreshToken)
                    .User(user)
                    .build();
            refreshTokenRepository.save(newRefreshToken);
        } else {
            RefreshToken newRefreshToken = oldRefreshToken.get().toBuilder()
                    .refreshToken(refreshToken)
                    .build();
            refreshTokenRepository.save(newRefreshToken);
        }

        return SignInResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .msg("로그인 성공")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken, String oldAccessToken) throws JsonProcessingException {
        tokenProvider.validateRefreshToken(refreshToken, oldAccessToken);

        String subject = tokenProvider.decodeJwtPayloadSubject(oldAccessToken);
        long userId = Long.parseLong(subject.split(":")[0]);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보가 없습니다. id=" + userId));

        String newAccess = tokenProvider.recreateAccessToken(oldAccessToken);
        String newRefresh = tokenProvider.createRefreshToken(user);

        RefreshToken entity = RefreshToken.builder()
                .tokenId(userId)
                .User(user)
                .refreshToken(newRefresh)
                .build();
        refreshTokenRepository.save(entity);

        return new TokenResponse(newAccess, newRefresh, null);
    }

    @Transactional
    public String signOut(String token) throws JsonProcessingException {
        String username = tokenProvider.validateTokenAndGetSubject(token).toLowerCase(Locale.getDefault());
        Optional<User> userOptional = userRepository.findByUserEmailOptional(username);
        if (userOptional.isEmpty() || refreshTokenRepository.findById(userOptional.get().getUserId()).isEmpty()) {
            return "로그아웃 실패";
        }

        try {
            refreshTokenRepository.deleteById(userOptional.get().getUserId());
        } catch (Exception e) {
            return "로그아웃 실패";
        }

        return "로그아웃 성공";
    }

    @Transactional
    public String deleteUser(String token, String password) {
        String subject = tokenProvider.validateTokenAndGetSubject(token);
        String userEmail = subject.split(":")[1];

        User user = userRepository.findByUserEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        if (!passwordEncoder.matches(password, user.getUserPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        refreshTokenRepository.deleteById(user.getUserId());

        kakaoUserRepository.findByUser(user).ifPresent(kakaoUserRepository::delete);
//        permissionRepository.deleteAllByUser(user);
//
//        List<Lists> userLists = listsRepository.findAllByUser(user);
//        for (Lists list : userLists) {
//            productRepository.deleteAllByLists(list); // 연결된 상품 먼저 제거
//            listsRepository.delete(list);
//        }

        userRepository.delete(user);

        return "회원 탈퇴 성공";
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,16}$";
        return Pattern.matches(passwordPattern, password);
    }

    // 카카오 로그인
    @Transactional
    public KakaoLoginResponse kakaoSignIn(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new RuntimeException("카카오 액세스 토큰이 유효하지 않습니다.");
        }

        log.info("Received Kakao access token: {}", accessToken);

        String kakaoId = kakaoService.extractKakaoId(accessToken);
        boolean isFirstKakao = kakaoUserRepository.findByKakaoId(kakaoId).isEmpty();
        KakaoUser kakaoUser = kakaoService.saveOrUpdateKakaoUser(accessToken);

        if (kakaoUser == null) {
            throw new RuntimeException("카카오 사용자 정보가 없습니다.");
        }

        User user = kakaoUser.getUser();
        userRepository.save(user);

        if (user == null) {
            throw new RuntimeException("해당 카카오 사용자에 대한 유저 정보가 없습니다.");
        }

        log.info("User associated with Kakao user: {}", user);

        String jwtAccessToken = tokenProvider.createToken(user);
        String jwtRefreshToken = tokenProvider.createRefreshToken(user);

        refreshTokenRepository.save(
                new RefreshToken(user.getUserId(), jwtRefreshToken, user));

        return KakaoLoginResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .msg("카카오 로그인 성공")
                .accessToken(jwtAccessToken)
                .refreshToken(jwtRefreshToken)
                .firstSocialLogin(isFirstKakao)
                .socialProvider("kakao")
                .build();
    }
}
