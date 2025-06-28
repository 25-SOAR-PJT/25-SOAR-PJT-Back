package org.project.soar.model.user;

import jakarta.persistence.*;
import lombok.*;
import org.project.soar.global.abstracts.BaseTimeEntity;
import java.time.LocalDate;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", length = 100, nullable = false)
    private String userName;

    @Column(name = "user_birth_date")
    private LocalDate userBirthDate;

    @Column(name = "user_phone_number", length = 20)
    private String userPhoneNumber;

    @Column(name = "user_gender", length = 10)
    private String userGender;

    @Column(name = "user_email", length = 100, unique = true)
    private String userEmail;

    @Column(name = "user_password", length = 255)
    private String userPassword;
}
