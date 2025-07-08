package org.project.soar.model.user.dto;

import java.sql.Date;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDto {
    private Long userId;
    private String userName;
    private Date userBirthDate;
    private String userPhoneNumber;
    private boolean userGender;
    private String userEmail;

    @Builder
    public UserDto(Long userId, String userName, Date userBirthDate, String userPhoneNumber, boolean userGender,
            String userEmail) {
        this.userId = userId;
        this.userName = userName;
        this.userBirthDate = userBirthDate;
        this.userPhoneNumber = userPhoneNumber;
        this.userGender = userGender;
        this.userEmail = userEmail;

    }

}
