package org.project.soar.config;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Order(0)
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String accessToken = parseBearerToken(req, HttpHeaders.AUTHORIZATION);

        try {
            if (accessToken != null) {
                User principal = parseUserSpecification(accessToken);
                setAuthentication(principal, accessToken, req);
                logger.info("[JwtFilter] 액세스토큰 인증 성공, 사용자: {}", principal.getUsername());
            }
            chain.doFilter(req, res);

        } catch (ExpiredJwtException e) {
            logger.info("[JwtFilter] 액세스토큰 만료 감지, 리프레시 시도");

            String refreshToken = parseBearerToken(req, "Refresh-Token");
            logger.info("    └ 클라이언트로부터 받은 Refresh-Token: {}", refreshToken); 

            try {
                tokenProvider.validateRefreshToken(refreshToken, accessToken);

                String newAccessToken = tokenProvider.recreateAccessToken(accessToken);
                logger.info("    └ 재발급된 새 액세스토큰: {}", newAccessToken); 

                res.setHeader("New-Access-Token", newAccessToken);

                // 새 토큰으로 다시 인증
                User newPrincipal = parseUserSpecification(newAccessToken);
                setAuthentication(newPrincipal, newAccessToken, req);

                chain.doFilter(req, res);
                return;
            } catch (Exception ex) {
                logger.error("리프레시로 재발급 실패", ex);
            }

            SecurityContextHolder.clearContext();
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었습니다.");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        logger.info("[shouldNotFilter] 요청 경로: {}", path);
        return path.startsWith("/api/auth/signin") || path.startsWith("/api/auth/signup") 
                || path.startsWith("/api/auth/refresh");
    }

    private String parseBearerToken(HttpServletRequest request, String headerName) {
        return Optional.ofNullable(request.getHeader(headerName))
                .filter(token -> token.length() > 7 && token.substring(0, 7).equalsIgnoreCase("Bearer "))
                .map(token -> token.substring(7))
                .orElse(null);
    }

    private User parseUserSpecification(String token) {
        String subject = Optional.ofNullable(token)
                .filter(t -> t.length() > 10)
                .map(tokenProvider::validateTokenAndGetSubject)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or missing token"));

        logger.info("Parsed subject from token: {}", subject);

        String[] split = subject.split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid token format. Expected format: 'userId:role'. Found: " + subject);
        }

        return new User(split[0], "", List.of(new SimpleGrantedAuthority(split[1])));
    }
    
    private void setAuthentication(User principal, String token, HttpServletRequest req) {
        AbstractAuthenticationToken auth = UsernamePasswordAuthenticationToken.authenticated(principal, token,
                principal.getAuthorities());
        auth.setDetails(new WebAuthenticationDetails(req));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void reissueAccessToken(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        try {
            String refreshToken = parseBearerToken(request, "Refresh-Token");
            if (refreshToken == null) {
                throw exception;
            }
            String oldAccessToken = parseBearerToken(request, HttpHeaders.AUTHORIZATION);
            tokenProvider.validateRefreshToken(refreshToken, oldAccessToken);
            String newAccessToken = tokenProvider.recreateAccessToken(oldAccessToken);
            User user = parseUserSpecification(newAccessToken);
            AbstractAuthenticationToken authenticated = UsernamePasswordAuthenticationToken.authenticated(user,
                    newAccessToken, user.getAuthorities());
            authenticated.setDetails(new WebAuthenticationDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticated);

            response.setHeader("New-Access-Token", newAccessToken);
        } catch (Exception e) {
            logger.error("Exception in reissuing access token: {}", e.getMessage());
            request.setAttribute("exception", e);
        }
    }
}