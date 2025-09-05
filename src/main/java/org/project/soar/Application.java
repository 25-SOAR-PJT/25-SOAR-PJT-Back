package org.project.soar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing  // BaseTimeEntity 사용을 위해 필수
@EnableScheduling   // 스케줄러 사용시 필요
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
