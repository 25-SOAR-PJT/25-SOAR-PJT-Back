package org.project.soar.model.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KakaoLoginResponse {
    private Long userId;
    private String userName;
    private String userEmail;
    private String msg;
    private String accessToken;
    private String refreshToken;
    private String kakaoAccessToken;
    private boolean firstSocialLogin;
    private String socialProvider;

    @Builder
    public KakaoLoginResponse(Long userId, String userName,String userEmail, String msg, String accessToken, String refreshToken, String kakaoAccessToken,
                          boolean firstSocialLogin, String socialProvider) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.msg = msg;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.kakaoAccessToken = kakaoAccessToken;
        this.firstSocialLogin = firstSocialLogin;
        this.socialProvider = socialProvider;
    }
}
