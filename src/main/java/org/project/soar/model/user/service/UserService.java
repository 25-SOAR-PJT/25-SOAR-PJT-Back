package org.project.soar.model.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.config.TokenProvider;
import org.project.soar.model.comment.repository.CommentRepository;
import org.project.soar.model.permission.repository.PermissionRepository;
import org.project.soar.model.permission.service.PermissionService;
import org.project.soar.model.tag.Tag;
import org.project.soar.model.user.KakaoUser;
import org.project.soar.model.user.RefreshToken;
import org.project.soar.model.user.User;
import org.project.soar.model.user.dto.*;
import org.project.soar.model.user.enums.Role;
import org.project.soar.model.user.repository.KakaoUserRepository;
import org.project.soar.model.user.repository.RefreshTokenRepository;
import org.project.soar.model.user.repository.UserRepository;
import org.project.soar.model.usertag.repository.UserTagRepository;
import org.project.soar.model.usertag.service.UserTagService;
import org.project.soar.model.youthpolicy.repository.UserYouthPolicyRepository;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.repository.YouthPolicyBookmarkRepository;
import org.project.soar.model.youthpolicytag.repository.YouthPolicyTagRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private final PermissionRepository permissionRepository;
    private final UserYouthPolicyRepository userYouthPolicyRepository;
    private final UserTagRepository userTagRepository;
    private final YouthPolicyTagRepository youthPolicyTagRepository;
    private final CommentRepository commentRepository;
    private final YouthPolicyBookmarkRepository youthPolicyBookmarkRepository;
    private final Random random = new Random();

    @Transactional
    public SignUpResponse signUp(SignUpRequest request, String otp, List<Boolean> agreedTerms) {
        String normalizedEmail = request.getUserEmail().trim().toLowerCase(Locale.getDefault());

        if (userRepository.existsByUserEmail(request.getUserEmail())) {
            return SignUpResponse.builder()
                    .msg("이메일이 이미 존재합니다.")
                    .build();
        }
        // 이메일 인증 코드 확인
        String authResult = emailService.checkAuthNumber(normalizedEmail, otp);
        if ("EXPIRED".equals(authResult)) {
            return SignUpResponse.builder()
                    .msg("인증번호가 만료되었습니다. 다시 요청해주세요.")
                    .build();
        }else if ("MISMATCH".equals(authResult)) {
            return SignUpResponse.builder()
                    .msg("인증번호를 다시 확인해주세요.")
                    .build();
        }

        // 비밀번호 보안 규칙 확인
        if (!isValidPassword(request.getUserPassword())) {
            return SignUpResponse.builder()
                    .msg("비밀번호는 8~20자로 영문 소문자, 숫자를 조합해서 사용해주세요.")
                    .build();
        }

        User user = User.builder()
                .userName(request.getUserName())
                .userBirthDate(request.getUserBirthDate().toLocalDate())
                .userPhoneNumber(request.getUserPhoneNumber())
                .userGender(request.isUserGender())
                .userEmail(request.getUserEmail())
                .userPassword(passwordEncoder.encode(request.getUserPassword()))
                .userRole(Role.USER)
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
                    .msg("이메일을 다시 확인해주세요.")
                    .build();
        }

        if (!passwordEncoder.matches(request.getUserPassword(), user.getUserPassword())) {
            return SignInResponse.builder()
                    .msg("비밀번호를 다시 확인해주세요.")
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
                    .user(user)
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
                .user(user)
                .refreshToken(newRefresh)
                .build();
        refreshTokenRepository.save(entity);

        return new TokenResponse(newAccess, newRefresh, null);
    }

    @Transactional
    public String signOut(String token) throws JsonProcessingException {
        String subject = tokenProvider.validateTokenAndGetSubject(token);
        String userEmail = subject.split(":")[1];

        User user = userRepository.findByUserEmail(userEmail);
        if (user == null) {
            return "사용자를 찾을 수 없습니다.";
        }
        if (refreshTokenRepository.findById(user.getUserId()).isEmpty()) {
            return "RefreshToken이 존재하지 않습니다.";
        }

        try {
            refreshTokenRepository.deleteById(user.getUserId());
        } catch (Exception e) {
            return "로그아웃 실패";
        }

        return "로그아웃 성공";
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

    @Transactional
    public boolean checkEmailExists(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.getDefault());
        return userRepository.existsByUserEmail(normalizedEmail);
    }

    @Transactional
    public FindIdResponse findId(String userName, LocalDate userBirthDate, boolean userGender) {
        List<User> users = userRepository.findByUserNameAndUserBirthDateAndUserGender(userName, userBirthDate, userGender);
        if (users == null || users.isEmpty()) {
            return null; // 해당 유저가 존재하지 않음
        }

        // 이메일 리스트 추출
        List<String> userEmails = users.stream()
                .map(User::getUserEmail)
                .collect(Collectors.toList());

        return new FindIdResponse(userEmails);
    }

    @Transactional
    public String findPassword(String userEmail, String userName) {
        User user = userRepository.findByUserEmail(userEmail);
        if (user == null || !user.getUserName().equals(userName)) {
            return "해당 이메일 또는 이름이 일치하는 사용자가 없습니다.";
        }

        String tempPassword = generateTemporaryPassword();
        user.updatePassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        emailService.sendTemporaryPassword(userEmail, tempPassword);
        return "임시 비밀번호가 이메일로 전송되었습니다.";
    }

    @Transactional
    public String updatePassword(String userEmail, String currentPassword, String newPassword) {
        User user = userRepository.findByUserEmail(userEmail);
        if (user == null) {
            return "해당 이메일로 등록된 사용자가 없습니다.";
        }

        if (!passwordEncoder.matches(currentPassword, user.getUserPassword())) {
            return "현재 비밀번호가 일치하지 않습니다.";
        }

        if (!isValidPassword(newPassword)) {
            return "비밀번호는 8~20자로 영문 소문자, 숫자를 조합해서 사용해주세요.";
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "비밀번호가 성공적으로 변경되었습니다.";
    }

    @Transactional
    public String updateUserName(Long userId, String newUserName) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        user.updateUserName(newUserName);
        userRepository.save(user);
        return "사용자 이름 업데이트 성공";
    }

    @Transactional
    public UserInfoResponse getUserInfo(User user, String userAddress) {
        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userAddress(userAddress)
                .build();
    }

    @Transactional
    public String deleteUser(String token, String password) {
        String subject = tokenProvider.validateTokenAndGetSubject(token);
        String userEmail = subject.split(":")[1];

        User user = userRepository.findByUserEmail(userEmail);
        if (user == null) {
            //throw new RuntimeException("사용자를 찾을 수 없습니다.");
            return "사용자를 찾을 수 없습니다.";
        }

        if (!passwordEncoder.matches(password, user.getUserPassword())) {
            //throw new RuntimeException("사용자를 찾을 수 없습니다.");
            return "비밀번호를 다시 확인해주세요.";
        }

        refreshTokenRepository.deleteById(user.getUserId());
        kakaoUserRepository.findByUser(user).ifPresent(kakaoUserRepository::delete);
        permissionRepository.deleteAllByUser(user);

        // 사용자와 연결된 coment,UserYouthPolicy,user_tag,북마크  삭제
        youthPolicyBookmarkRepository.findAllByUser(user).forEach(youthPolicyBookmarkRepository::delete);
        commentRepository.deleteAllByUser(user);
        userYouthPolicyRepository.deleteAllByUser(user);
        userTagRepository.deleteAllByUser(user);

        userRepository.delete(user);

        return "회원 탈퇴 성공";
    }

    @Transactional
    public String deleteKakaoUser(String token) {
        String subject = tokenProvider.validateTokenAndGetSubject(token);
        String userEmail = subject.split(":")[1];
        log.info("탈퇴 시작: {}", userEmail);
        User user = userRepository.findByUserEmail(userEmail);
        if (user == null) {
            return "사용자를 찾을 수 없습니다.";
        }

        Optional<KakaoUser> optionalKakaoUser = kakaoUserRepository.findByUser(user);
        if (optionalKakaoUser.isEmpty()) {
            return "카카오 유저 정보가 없습니다.";
        }

        KakaoUser kakaoUser = optionalKakaoUser.get();

        if (kakaoUser.getAccessToken() != null && !kakaoUser.getAccessToken().isEmpty()) {
            kakaoService.unlink(kakaoUser.getAccessToken());
        }

        // 카카오 사용자 정보 삭제
        kakaoUserRepository.delete(kakaoUser);
        permissionRepository.deleteAllByUser(user);

        // 사용자와 연결된 댓글, 청소년 정책, 유저 태그, 북마크 삭제
        youthPolicyBookmarkRepository.findAllByUser(user).forEach(youthPolicyBookmarkRepository::delete);
        commentRepository.deleteAllByUser(user);
        userYouthPolicyRepository.deleteAllByUser(user);
        userTagRepository.deleteAllByUser(user);

        refreshTokenRepository.deleteById(user.getUserId());
        userRepository.delete(user);

        return "카카오 사용자 삭제 성공";
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[a-z])(?=.*\\d).{8,20}$";
        return Pattern.matches(passwordPattern, password);
    }

    private String generateTemporaryPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password;
        do {
            password = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                password.append(characters.charAt(random.nextInt(characters.length())));
            }
        } while (!isValidPassword(password.toString())); // 규칙 만족할 때까지 반복
        return password.toString();
    }

    public MatchYouthPoliciesResponse getMatchPolicies(Long userId) {
        List<Tag> tags =  userTagRepository.findAllTagByUserId(userId);
        List<Long> tagIds = tags.stream().map(tag -> tag.getTagId()).collect(Collectors.toList());
        List<YouthPolicy> youthPolicies = youthPolicyTagRepository.findByTagIds(tagIds);
        return new MatchYouthPoliciesResponse(userId, tags, youthPolicies);
    }
}
