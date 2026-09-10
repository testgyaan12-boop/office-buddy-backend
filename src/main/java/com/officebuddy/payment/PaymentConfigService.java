package com.officebuddy.payment;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConfigService {

    private final PaymentConfigRepository repo;

    @PostConstruct
    public void ensureDefaults() {
        seed(PaymentConfig.RAZORPAY, "Razorpay gateway credentials");
        seed(PaymentConfig.PHONEPE, "PhonePe gateway credentials");
        seed(PaymentConfig.STRIPE, "Stripe gateway credentials");
    }

    private void seed(String provider, String description) {
        try {
            if (repo.findByProvider(provider).isEmpty()) {
                var cfg = new PaymentConfig();
                cfg.setProvider(provider);
//                cfg.setDescription(description);
                cfg.setIsActive(0);
                repo.save(cfg);
                log.info("Seeded payment_config provider={} (inactive, keys empty)", provider);
            }
        } catch (Exception e) {
            log.warn("Failed to seed payment_config {}: {}", provider, e.getMessage());
        }
    }

    public Optional<PaymentConfig> findByProvider(String provider) {
        try {
            return repo.findByProvider(provider);
        } catch (Exception e) {
            log.warn("payment_config read failed for {}: {}", provider, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<PaymentConfig> usableProvider(String provider) {
        return findByProvider(provider).filter(PaymentConfig::isUsable);
    }

    public Optional<PaymentConfig> activeProvider() {
        try {
            return repo.findByIsActiveAndIsDeletedOrderByIdAsc(1, 0).stream()
                    .filter(c -> c.getKeyId() != null && !c.getKeyId().isBlank())
                    .findFirst();
        } catch (Exception e) {
            log.warn("payment_config active lookup failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
