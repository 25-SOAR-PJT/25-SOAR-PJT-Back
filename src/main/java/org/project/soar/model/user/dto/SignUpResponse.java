package org.project.soar.model.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SignUpResponse {

    private final String msg;

    @Builder
    public SignUpResponse(String msg) {
        this.msg = msg;
    }
}
