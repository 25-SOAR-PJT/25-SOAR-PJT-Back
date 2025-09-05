package org.project.soar.model.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserInfoResponse {
    private Long userId;
    private String userName;
    private String userAddress;

    @Builder
    public UserInfoResponse(Long userId, String userName, String userAddress) {
        this.userId = userId;
        this.userName = userName;
        this.userAddress = userAddress;
    }
}
