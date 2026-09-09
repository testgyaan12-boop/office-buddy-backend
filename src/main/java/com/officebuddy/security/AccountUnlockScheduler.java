package com.officebuddy.security;

import com.officebuddy.auth.EmailService;
import com.officebuddy.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountUnlockScheduler {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecurityConfigService securityConfigService;

    @Scheduled(fixedDelay = 3 * 60 * 60 * 1000)
    public void unlockExpiredAccounts() {
        var expired = userRepository.findByAccountLockedUntilIsNotNullAndAccountLockedUntilBefore(LocalDateTime.now());
        if (expired.isEmpty()) return;
        int unlocked = 0;
        boolean sendMail = securityConfigService.isEnabled(SecurityConfigService.LOGIN_UNLOCK_EMAIL, true);
        for (var user : expired) {
            try {
                user.setAccountLockedUntil(null);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
                unlocked++;
                if (sendMail) {
                    try {
                        String resp = emailService.sendAccountUnlockedEmail(user.getEmail(), user.getName());
                        log.info("Account unlock email response for {}: {}", user.getEmail(), resp);
                    } catch (Exception e) {
                        log.warn("Account unlock email failed for {}: {}", user.getEmail(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to unlock account {}: {}", user.getEmail(), e.getMessage());
            }
        }
        log.info("AccountUnlockScheduler: unlocked {}/{} expired accounts", unlocked, expired.size());
    }
}
