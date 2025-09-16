package org.project.soar.model.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UserDetailInfoResponse {
    private Long userId;
    private String userName;
    private LocalDate userBirthDate;
    private Boolean userGender;



    @Builder
    public UserDetailInfoResponse(Long userId, String userName, LocalDate userBirthDate, Boolean userGender) {
        this.userId = userId;
        this.userName = userName;
        this.userBirthDate = userBirthDate;
        this.userGender = userGender;
    }
}
