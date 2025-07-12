package org.project.soar.model.user.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FindPasswordRequest {
    private String userEmail;
    private String userName;
    private String inputOtp ;


    @Builder
    public FindPasswordRequest(String userEmail, String userName) {
        this.userEmail = userEmail;
        this.userName = userName;
    }
    
}
