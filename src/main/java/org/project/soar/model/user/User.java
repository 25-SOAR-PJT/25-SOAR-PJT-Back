package org.project.soar.model.user;

import jakarta.persistence.*;
import lombok.*;
import org.project.soar.global.abstracts.BaseTimeEntity;
import org.project.soar.model.permission.Permission;
import org.project.soar.model.user.enums.Role;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
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
    private boolean userGender;

    @Column(name = "user_email", length = 100, unique = true)
    private String userEmail;

    @Column(name = "user_password", length = 255)
    private String userPassword;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Permission> permissions = new ArrayList<>();

    @Column(name = "user_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role userRole;

    public void updatePassword(String userPassword) {
        this.userPassword = userPassword;
    }
    public void updateUserName(String userName) {
        this.userName = userName;
    }
    public void updateUserRole(Role role) {
        this.userRole = role;
    }
}
