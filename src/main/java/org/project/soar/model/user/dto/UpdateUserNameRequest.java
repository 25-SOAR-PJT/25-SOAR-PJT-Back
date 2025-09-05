package org.project.soar.model.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateUserNameRequest {
    private Long userId;
    private String userName;

    public UpdateUserNameRequest(Long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }
}
