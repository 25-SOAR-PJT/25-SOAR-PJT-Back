package org.project.soar.model.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FindIdResponse {
    private String userEmail;

    public FindIdResponse(String userEmail) {
        this.userEmail = userEmail;
    }
}
