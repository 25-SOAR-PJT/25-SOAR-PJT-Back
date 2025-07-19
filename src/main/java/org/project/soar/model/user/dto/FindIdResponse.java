package org.project.soar.model.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FindIdResponse {
    private List<String> emails;

    public FindIdResponse(List<String> userEmails) {
        this.emails = userEmails;
    }
}
