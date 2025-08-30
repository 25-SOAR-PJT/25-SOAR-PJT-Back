package org.project.soar.model.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePasswordResponse {
    private String newPassword;
    private String confirmPassword;

    public UpdatePasswordResponse(String newPassword, String confirmPassword) {
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
}
