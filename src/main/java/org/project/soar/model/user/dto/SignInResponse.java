package org.project.soar.model.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SignInResponse {
    private Long userId;
    private String userName;
    private String userEmail;
    private String msg;
    private String accessToken;
    private String refreshToken;
    private String kakaoAccessToken;

    @Builder
    public SignInResponse(Long userId, String userName,String userEmail, String msg, String accessToken, String refreshToken, String kakaoAccessToken) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.msg = msg;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.kakaoAccessToken = kakaoAccessToken;
    }
    
}
