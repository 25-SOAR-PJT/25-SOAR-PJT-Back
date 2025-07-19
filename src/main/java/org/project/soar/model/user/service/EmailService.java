package org.project.soar.model.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.project.soar.config.RedisUtil;
import org.project.soar.model.user.repository.UserRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;
    private static final String FROM_EMAIL = "noreply@example.com";
    private final UserRepository userRepository;

    /**
     * 인증번호 생성 및 이메일 전송
     * 
     * @param email 수신자 이메일
     * @return 생성된 인증번호
     */
    public String setEmail(String email) {
        if (!isValidEmail(email)) {
            return "이메일 형식을 다시 확인해주세요.";
        }

        if(userRepository.existsByUserEmail(email)){
            return "이미 존재하는 이메일입니다.";
        }

        String authCode = generateAuthCode();
        String title = "이메일 인증 번호 안내";
        String content = generateEmailContent(authCode);

        sendMail(FROM_EMAIL, email, title, content);
        //redisUtil.setDataExpire(authCode, email, 60 * 5L); // 인증코드 5분 TTL
        redisUtil.setDataExpire(email, authCode, 60 * 5L);
        return authCode;
    }

    /**
     * 이메일 유효성 검증
     * 
     * @param email 이메일 주소
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return email.matches(emailRegex);
    }

    /**
     * 인증번호 생성
     * 
     * @return 4자리 숫자 인증번호
     */
    private String generateAuthCode() {
        StringBuilder key = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 4; i++) {
            key.append(rand.nextInt(10)); // 0~9
        }
        return key.toString();
    }

    /**
     * 이메일 본문 생성
     * 
     * @param code 인증번호
     * @return 이메일 본문 내용
     */
    private String generateEmailContent(String code) {
        return "<div style='margin:100px;'>"
                + "<h1> 안녕하세요. SOAR입니다. </h1>"
                + "<p>아래 인증 번호를 회원가입 창에 입력해주세요.</p>"
                + "<br><p>감사합니다!</p>"
                + "<div align='center' style='border:1px solid black; font-family:verdana;'>"
                + "<h3 style='color:blue;'>이메일 인증 코드</h3>"
                + "<div style='font-size:130%;'>CODE: <strong>" + code + "</strong></div>"
                + "</div>";
    }

    /**
     * 이메일 전송
     * 
     * @param from    발신자 이메일
     * @param to      수신자 이메일
     * @param title   제목
     * @param content 본문
     */
    @Transactional
    public void sendMail(String from, String to, String title, String content) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(title);
            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 인증번호 검증
     * 
     * @param email      이메일 주소
     * @param authNumber 인증번호
     * @return 검증 결과
     */
    @Transactional(readOnly = true)
    public String checkAuthNumber(String email, String authNumber) {
        String storedCode = redisUtil.getData(email);
//        return storedEmail != null && storedEmail.equals(email);
        if (storedCode == null) {
            return "EXPIRED";
        }

        if (!storedCode.equals(authNumber)) {
            return "MISMATCH";
        }

        return "SUCCESS";
    }

    /**
     *  임시 비밀번호 이메일 전송
     * 
     * @param email        수신자 이메일
     * @param tempPassword 생성된 임시 비밀번호
     */
    @Transactional
    public void sendTemporaryPassword(String email, String tempPassword) {
        if (!isValidEmail(email)) {
            return ;
        }

        String title = "임시 비밀번호 안내";
        String content = generateTemporaryPasswordEmailContent(tempPassword);

        sendMail(FROM_EMAIL, email, title, content);
    }

    /**
     *  임시 비밀번호 이메일 본문 생성
     * 
     * @param tempPassword 임시 비밀번호
     * @return 이메일 본문 내용
     */
    private String generateTemporaryPasswordEmailContent(String tempPassword) {
        return "<div style='margin:100px;'>"
                + "<h1>안녕하세요. SOAR입니다.</h1>"
                + "<p>요청하신 임시 비밀번호가 생성되었습니다. 아래의 비밀번호를 사용하여 로그인 후 반드시 변경해 주세요.</p>"
                + "<br>"
                + "<div align='center' style='border:1px solid black; font-family:verdana;'>"
                + "<h3 style='color:blue;'>임시 비밀번호</h3>"
                + "<div style='font-size:130%;'><strong>" + tempPassword + "</strong></div>"
                + "</div>"
                + "<br><p>감사합니다!</p>"
                + "</div>";
    }

}
