package org.project.soar.model.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequestDto {

    @Email(message = "유효하지 않은 이메일 형식입니다.")
    @NotEmpty(message = "이메일은 비워둘 수 없습니다.")
    private String email;
}
