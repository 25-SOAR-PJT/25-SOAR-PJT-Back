package org.project.soar.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "KakaoUser")
@Data
@NoArgsConstructor
public class KakaoUser {
    @Id
    @Column(name = "kakao_user_id")
    private String kakaoUserId;

    @Column(name = "kakao_id")
    private String kakaoId;

    @Column(name = "kakao_nickname")
    private String kakaoNickname;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "access_token")
    private String accessToken;

    @Builder
    public KakaoUser(String kakaoUserId, String kakaoId, String kakaoNickname, User user, String accessToken) {
        this.kakaoUserId = kakaoUserId;
        this.kakaoId = kakaoId;
        this.kakaoNickname = kakaoNickname;
        this.user = user;
        this.accessToken = accessToken;
    }
}
