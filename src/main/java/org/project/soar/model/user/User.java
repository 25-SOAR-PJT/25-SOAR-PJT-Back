package org.project.soar.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "User")
@Data
public class User {
    @Id
    @Column(name = "user_id")
    private Integer userId;

    private String userName;
    private String userBirthDate;
    private String userPhoneNumber;
    private String userGender;
    private String userEmail;
    private String userPassword;
}
