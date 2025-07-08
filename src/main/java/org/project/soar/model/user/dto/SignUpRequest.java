package org.project.soar.model.user.dto;

import java.sql.Date;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SignUpRequest {

    private final String userName;
    private final Date userBirthDate;
    private final String userPhoneNumber;
    private final boolean userGender;
    private final String userEmail;
    private final String userPassword;
    private final String otp;
    private final List<Boolean> agreedTerms;

    @Builder
    public SignUpRequest(String userName, Date userBirthDate, String userPhoneNumber, boolean userGender, String userEmail, String userUsername, String userPassword, String otp, List<Boolean> agreedTerms) {
        this.userName = userName;
        this.userBirthDate = userBirthDate;
        this.userPhoneNumber = userPhoneNumber;
        this.userGender = userGender;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.otp = otp;
        this.agreedTerms = agreedTerms;
    }
}
