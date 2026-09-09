package com.officebuddy.adAndSubscription.subscription.plan;

import com.officebuddy.adAndSubscription.subscription.plan.entity.Plan;
import com.officebuddy.adAndSubscription.subscription.plan.repository.PlanRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlanSeeder {

    private final PlanRepository repo;

    @PostConstruct
    public void seed() {
        seed("Free", "FREE", "LIFETIME", 104857600L, "MB");
        seed("Monthly", "PRO_MONTHLY", "MONTHLY", 524288000L, "MB");
        seed("Yearly", "PRO_YEARLY", "YEARLY", 2147483648L, "GB");
    }

    private void seed(String name, String code, String period, Long bytes, String unit) {
        try {
            if (repo.findByPlanCode(code).isEmpty()) {
                repo.save(new Plan(name, code, period, bytes, unit));
                log.info("Seeded plan {} ({} {})", code, bytes, unit);
            }
        } catch (Exception e) {
            log.warn("Failed to seed plan {}: {}", code, e.getMessage());
        }
    }
}
