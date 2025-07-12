package org.project.soar.model.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePasswordRequest {
    private String userEmail;
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

    public UpdatePasswordRequest(String userEmail, String currentPassword, String newPassword, String confirmPassword) {
        this.userEmail = userEmail;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
}
