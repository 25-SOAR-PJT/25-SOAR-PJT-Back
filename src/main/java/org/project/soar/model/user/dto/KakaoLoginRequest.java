package org.project.soar.model.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class KakaoLoginRequest {
    @JsonProperty("access_token")
    private String accessToken; // 카카오에서 받은 액세스 토큰

    public KakaoLoginRequest(String accessToken) {
        this.accessToken = accessToken;
    }
}
