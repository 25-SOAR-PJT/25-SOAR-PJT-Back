package org.project.soar.model.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomMemberDto {
    private Long memberId;
    private String email;
    private String role;
    private String memberPassword;
    private String memberUsername;
}
