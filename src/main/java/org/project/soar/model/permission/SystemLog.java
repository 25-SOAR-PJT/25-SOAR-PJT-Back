//package org.project.soar.model.auth;
//
//import jakarta.persistence.*;
//import lombok.AccessLevel;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import org.project.soar.model.user.User;
//
//import java.sql.Timestamp;
//
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@Getter
//@Entity
//@Table(name = "system_log") // 시스템 로그 테이블
//public class SystemLog {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "sl_id", nullable = false) // 로그 ID
//    private Long slId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false) // 사용자 ID
//    private User user;
//
//    @Column(name = "action", nullable = false, length = 255) // 액션
//    private String action;
//
//    @Column(name = "timestamp", nullable = false) // 발생 시간
//    private Timestamp timestamp;
//
//    @Builder
//    public SystemLog(Long slId, User user, String action, Timestamp timestamp) {
//        this.slId = slId;
//        this.user = user;
//        this.action = action;
//        this.timestamp = timestamp;
//    }
//}
