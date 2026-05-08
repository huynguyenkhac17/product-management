package com.example.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    // Bean này trả về "ai đang thực hiện hành động" để JPA Auditing tự điền
    // vào các cột creator_id / last_updated_id trong database.
    // Tạm thời trả cứng 1L (user id = 1).
    // Thực tế: lấy từ JWT token hoặc SecurityContext.
    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> Optional.of(1L);
    }
}
