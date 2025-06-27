package org.project.soar.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "KakaoUser")
@Data
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
}
