package org.project.soar.model.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

//import java.util.Date;
import java.sql.Date;

@Getter
@NoArgsConstructor
public class FindIdRequest {
    private String userName;
    private Date userBirthDate;

    public FindIdRequest(String userName, Date userBirthDate) {
        this.userName = userName;
        this.userBirthDate = userBirthDate;
    }
}
