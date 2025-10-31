package org.project.soar.model.user.dto;

import java.sql.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SignUpRequest {

    private final String userName;
    private final Date userBirthDate;
    private final String userPhoneNumber;
    private final Boolean userGender;
    private final String userEmail;
    private final String userPassword;
    private final String otp;
    private final List<Boolean> agreedTerms;

    @Builder
    @JsonCreator
    public SignUpRequest(@JsonProperty("userName") String userName,
                         @JsonProperty("userBirthDate") Date userBirthDate,
                         @JsonProperty("userPhoneNumber") String userPhoneNumber,
                         @JsonProperty("userGender") boolean userGender,
                         @JsonProperty("userEmail") String userEmail,
                         @JsonProperty("userPassword") String userPassword,
                         @JsonProperty("otp") String otp,
                         @JsonProperty("agreedTerms") List<Boolean> agreedTerms) {
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
