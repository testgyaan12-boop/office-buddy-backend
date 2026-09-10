package com.officebuddy.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityConfigService {

    public static final String LOGIN_MAX_ATTEMPTS = "login.max_failed_attempts";
    public static final String LOGIN_LOCK_HOURS = "login.lock_duration_hours";
    public static final String LOGIN_LOCK_EMAIL = "login.lock_email_enabled";
    public static final String LOGIN_UNLOCK_EMAIL = "login.unlock_email_enabled";

    private final SecuritySettingRepository repo;

    @PostConstruct
    public void ensureDefaults() {
        seed(LOGIN_MAX_ATTEMPTS, "5", "Failed logins before account lock");
        seed(LOGIN_LOCK_HOURS, "12", "Account lock duration in hours");
        seed(LOGIN_LOCK_EMAIL, "1", "Send email on account lock (1/0)");
        seed(LOGIN_UNLOCK_EMAIL, "1", "Send email on account unlock (1/0)");
    }

    private void seed(String key, String value, String description) {
        try {
            if (repo.findByConfigKey(key).isEmpty()) {
                var setting = SecuritySetting.builder()
                        .configKey(key)
                        .configValue(value)
                        .build();
                setting.setDescription(description);
                repo.save(setting);
                log.info("Seeded security_config {}={}", key, value);
            }
        } catch (Exception e) {
            log.warn("Failed to seed security_config {}: {}", key, e.getMessage());
        }
    }

    public String getString(String key, String defaultValue) {
        try {
            return repo.findByConfigKey(key)
                    .filter(s -> Integer.valueOf(1).equals(s.getIsActive()) && !Integer.valueOf(1).equals(s.getIsDeleted()))
                    .map(SecuritySetting::getConfigValue)
                    .orElse(defaultValue);
        } catch (Exception e) {
            log.warn("security_config read failed for {}: {}", key, e.getMessage());
            return defaultValue;
        }
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getString(key, String.valueOf(defaultValue)).trim());
        } catch (Exception e) {
            log.warn("security_config bad int for {}: {}", key, e.getMessage());
            return defaultValue;
        }
    }

    public boolean isEnabled(String key, boolean defaultValue) {
        String v = getString(key, defaultValue ? "1" : "0").trim();
        return "1".equals(v) || "true".equalsIgnoreCase(v) || "yes".equalsIgnoreCase(v);
    }

    public List<SecuritySetting> listAll() {
        return repo.findAll();
    }
}
