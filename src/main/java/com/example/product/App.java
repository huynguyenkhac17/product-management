package com.example.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    // Lấy userId hiện tại từ SecurityContext (JwtAuthFilter set principal = Long userId).
    // Nếu chưa đăng nhập (vd: self-register), trả Optional.empty() → JPA bỏ qua audit field.
    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return Optional.empty();
            Object principal = auth.getPrincipal();
            return (principal instanceof Long id) ? Optional.of(id) : Optional.empty();
        };
    }
}
