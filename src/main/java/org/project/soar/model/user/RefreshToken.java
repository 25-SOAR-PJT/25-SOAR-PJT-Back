package org.project.soar.model.user;

import jakarta.persistence.*;
import lombok.*;
import org.project.soar.global.abstracts.BaseTimeEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "refresh_token")
@Getter
public class RefreshToken extends BaseTimeEntity {

    @Id
    @Column(name = "token_id")
    private Long tokenId;

    @Column(name = "refresh_token", length = 500, nullable = false)
    private String refreshToken;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User User;

    @Builder(toBuilder = true)
    public RefreshToken(Long tokenId, String refreshToken,User User) {
        this.tokenId = tokenId;
        this.refreshToken = refreshToken;
        this.User = User;
    }

    public boolean validateRefreshToken(String refreshToken) {
        return this.refreshToken.equals(refreshToken);
    }
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}