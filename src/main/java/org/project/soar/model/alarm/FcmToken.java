package org.project.soar.model.alarm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private String fcmToken;

    @Builder
    public FcmToken(Long userId, String fcmToken) {
        this.userId = userId;
        this.fcmToken = fcmToken;
    }
}
