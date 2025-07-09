package org.project.soar.model.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "KakaoUser")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kakao_user_id")
    private Long kakaoUserId;

    @Column(name = "kakao_id", nullable = false, unique = true)
    private String kakaoId;

    @Column(name = "kakao_nickname")
    private String kakaoNickname;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "access_token")
    private String accessToken;

    @Builder
    public KakaoUser(String kakaoId, String kakaoNickname, User user, String accessToken) {
        this.kakaoId = kakaoId;
        this.kakaoNickname = kakaoNickname;
        this.user = user;
        this.accessToken = accessToken;
    }
}
