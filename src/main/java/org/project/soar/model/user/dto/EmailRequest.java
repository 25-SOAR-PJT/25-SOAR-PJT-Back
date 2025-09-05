package org.project.soar.model.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
public class EmailRequest {

    @NotEmpty(message = "이메일은 비워둘 수 없습니다.")
    private String email;
}
