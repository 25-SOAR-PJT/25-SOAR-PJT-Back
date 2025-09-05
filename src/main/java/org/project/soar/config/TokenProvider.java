package org.project.soar.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import org.project.soar.model.user.RefreshToken;
import org.project.soar.model.user.User;
import org.project.soar.model.user.repository.RefreshTokenRepository;
import org.project.soar.model.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
public class TokenProvider {
    private final String secretKey;
    private final long expirationMinutes;
    private final long refreshExpirationHours;
    private final String issuer;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    public TokenProvider(
            @Value("${secret-key}") String secretKey,
            @Value("${expiration-minutes}") long expirationMinutes,
            @Value("${refresh-expiration-hours}") long refreshExpirationHours,
            @Value("${issuer}") String issuer,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository) {
        this.secretKey = secretKey;
        this.expirationMinutes = expirationMinutes;
        this.refreshExpirationHours = refreshExpirationHours;
        this.issuer = issuer;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public String createToken(User user) {
        String userSpecification = user.getUserId() + ":" + user.getUserEmail();
        return Jwts.builder()
                .signWith(new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS512.getJcaName()))
                .setSubject(userSpecification) // "userId:userEmail"
                .claim("role", user.getUserRole().name())
                .setIssuer(issuer)
                .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
                .setExpiration(Date.from(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)))
                .compact();
    }

    public String createRefreshToken(User user) {
        String subject = user.getUserId() + ":" + user.getUserEmail();
        return Jwts.builder()
                .signWith(new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS512.getJcaName()))
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
                .setExpiration(Date.from(Instant.now().plus(refreshExpirationHours, ChronoUnit.HOURS)))
                .compact();
    }

    public String validateTokenAndGetSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @Transactional
    public String recreateAccessToken(String oldAccessToken) throws JsonProcessingException {
        logger.debug("[TokenProvider] recreateAccessToken 호출 → oldAccessToken={}", oldAccessToken);
        String subject = decodeJwtPayloadSubject(oldAccessToken);
        Optional<RefreshToken> oldRefreshToken = refreshTokenRepository.findById(Long.parseLong(subject.split(":")[0]));
        if (oldRefreshToken.isEmpty()) {
            throw new ExpiredJwtException(null, null, "Refresh token expired.");
        }
        return createTokenFromSubject(subject);
    }

    private String createTokenFromSubject(String subject) {
        logger.debug("[TokenProvider] 새로 생성된 액세스토큰: {}", subject);
        return Jwts.builder()
                .signWith(new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS512.getJcaName()))
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
                .setExpiration(Date.from(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)))
                .compact();
    }

    @Transactional(readOnly = true)
    public void validateRefreshToken(String refreshToken, String oldAccessToken) throws JsonProcessingException {
        logger.debug("[TokenProvider] validateRefreshToken 호출 → refreshToken={}, oldAccessToken={}", refreshToken,
                oldAccessToken);
        validateAndParseToken(refreshToken);
        String userId = decodeJwtPayloadSubject(oldAccessToken).split(":")[0];
        refreshTokenRepository.findById(Long.parseLong(userId))
                .filter(RefreshToken -> RefreshToken.validateRefreshToken(refreshToken))
                .orElseThrow(() -> new ExpiredJwtException(null, null, "Refresh token expired."));
    }

    private Jws<Claims> validateAndParseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token);
    }

    public String decodeJwtPayloadSubject(String oldAccessToken) throws JsonProcessingException {
        return objectMapper.readValue(
                new String(Base64.getDecoder().decode(oldAccessToken.split("\\.")[1]), StandardCharsets.UTF_8),
                Map.class).get("sub").toString();
    }

    public String extractUserEmail(String token) throws JsonProcessingException {
        String subject = decodeJwtPayloadSubject(token); // e.g. "7:3919161577@kakao.com"
        return subject.split(":")[1]; // "3919161577@kakao.com"
    }

    public Map<String, Object> decodeGoogleIdToken(String idToken) {
        try {
            return objectMapper.readValue(
                    new String(Base64.getDecoder().decode(idToken.split("\\.")[1]), StandardCharsets.UTF_8),
                    Map.class);
        } catch (Exception e) {
            throw new RuntimeException("ID Token 파싱 실패", e);
        }
    }

    public Claims parseAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /*
     * ===========================
     * 👇 추가된 인증 헬퍼 메서드
     * ===========================
     */

    /** Authorization 헤더에서 Bearer 토큰 추출 */
    public String extractAccessToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    /** 토큰에서 userId(Long)만 추출 */
    public Long extractUserIdFromToken(String token) {
        if (token == null)
            return null;
        try {
            String subject = validateTokenAndGetSubject(token); // "userId:userEmail"
            String[] parts = subject.split(":");
            if (parts.length < 1)
                return null;
            return Long.parseLong(parts[0]);
        } catch (Exception e) {
            return null;
        }
    }

    /** 토큰에서 User 엔티티 조회 (없으면 null) */
    public User getUserFromToken(String token) {
        Long userId = extractUserIdFromToken(token);
        if (userId == null)
            return null;
        try {
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /** 요청에서 User 엔티티 바로 조회 (없으면 null) */
    public User getUserFromRequest(HttpServletRequest request) {
        String token = extractAccessToken(request);
        return getUserFromToken(token);
    }
}
