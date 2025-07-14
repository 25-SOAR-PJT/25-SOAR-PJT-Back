package org.project.soar.model.user.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDto {
    private Long userId;
    private String userName;
    private LocalDate userBirthDate;
    private String userPhoneNumber;
    private boolean userGender;
    private String userEmail;

    @Builder
    public UserDto(Long userId, String userName, LocalDate userBirthDate, String userPhoneNumber, boolean userGender,
                   String userEmail) {
        this.userId = userId;
        this.userName = userName;
        this.userBirthDate = userBirthDate;
        this.userPhoneNumber = userPhoneNumber;
        this.userGender = userGender;
        this.userEmail = userEmail;

    }

}
