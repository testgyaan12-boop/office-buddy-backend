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
        seed("Free", "FREE", "LIFETIME", 104857600L, "MB", 0L, "INR");
        seed("Monthly", "PRO_MONTHLY", "MONTHLY", 524288000L, "MB", 99L, "INR");
        seed("Yearly", "PRO_YEARLY", "YEARLY", 2147483648L, "GB", 999L, "INR");
    }

    private void seed(String name, String code, String period, Long bytes, String unit, Long amount, String currency) {
        try {
            var existing = repo.findByPlanCode(code).orElse(null);
            if (existing == null) {
                repo.save(new Plan(name, code, period, bytes, unit, amount, currency));
                log.info("Seeded plan {} ({} {}, {} {})", code, bytes, unit, amount, currency);
            } else if (existing.getAllocatedBytes() == null || existing.getAmount() == null) {
                existing.setAllocatedBytes(bytes);
                existing.setAllocatedUnit(unit);
                existing.setAmount(amount);
                existing.setCurrency(currency);
                repo.save(existing);
                log.info("Backfilled plan {} pricing/storage", code);
            }
        } catch (Exception e) {
            log.warn("Failed to seed plan {}: {}", code, e.getMessage());
        }
    }
}
