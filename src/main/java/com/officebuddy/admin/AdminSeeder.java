package com.officebuddy.admin;

import com.officebuddy.user.User;
import com.officebuddy.user.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@officebuddy.app}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    @PostConstruct
    public void seed() {
        try {
            var existing = userRepository.findByEmail(adminEmail).orElse(null);
            if (existing != null) {
                if (!"admin".equalsIgnoreCase(existing.getAccessRole())) {
                    existing.setAccessRole("admin");
                    userRepository.save(existing);
                    log.info("Promoted {} to admin", adminEmail);
                }
                return;
            }
            var admin = User.builder()
                    .name("Administrator")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .emailVerified(true)
                    .accessRole("admin")
                    .build();
            userRepository.save(admin);
            log.warn("Seeded super-admin {} — change the default password immediately", adminEmail);
        } catch (Exception e) {
            log.warn("Admin seed skipped: {}", e.getMessage());
        }
    }
}
