package com.officebuddy.adAndSubscription.subscription.service.impl;

import com.officebuddy.adAndSubscription.subscription.dto.SubscriptionDto;
import com.officebuddy.adAndSubscription.subscription.entity.Subscription;
import com.officebuddy.adAndSubscription.subscription.plan.repository.PlanRepository;
import com.officebuddy.adAndSubscription.subscription.repository.SubscriptionRepository;
import com.officebuddy.adAndSubscription.subscription.service.RazorPayService;
import com.officebuddy.adAndSubscription.subscription.service.SubscriptionService;
import com.officebuddy.storage.quota.service.StorageQuotaService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository repo;
    private final RazorPayService razorPayService;
    private final StorageQuotaService quotaService;
    private final PlanRepository planRepo;

    @Value("${razorpay.key-id:}")
    private String keyId;

    private long limitFor(String code) {
        try {
            var plan = planRepo.findByPlanCode(code).orElse(null);
            if (plan != null && plan.getAllocatedBytes() != null) return plan.getAllocatedBytes();
        } catch (Exception ignored) {}
        if ("PRO_YEARLY".equals(code)) return 10737418240L;
        if ("PRO_MONTHLY".equals(code)) return 5368709120L;
        return 209715200L;
    }
    private Integer maxCompaniesFor(String code) {
        if ("PRO_YEARLY".equals(code)) return 50;
        if ("PRO_MONTHLY".equals(code)) return 20;
        return 5;
    }
    private Integer maxDocsFor(String code) {
        if ("PRO_YEARLY".equals(code)) return 500;
        if ("PRO_MONTHLY".equals(code)) return 200;
        return 50;
    }

    private SubscriptionDto toDto(Subscription s) {
        return SubscriptionDto.builder()
                .id(s.getId().toString())
                .userId(s.getUserId().toString())
                .planCode(s.getPlanCode())
                .planName(s.getPlanName())
                .storageLimitBytes(s.getStorageLimitBytes())
                .maxCompanies(s.getMaxCompanies())
                .maxDocuments(s.getMaxDocuments())
                .adsEnabled(s.getAdsEnabled())
                .status(s.getStatus())
                .startDate(s.getStartDate() != null ? s.getStartDate().toString() : null)
                .expiryDate(s.getExpiryDate() != null ? s.getExpiryDate().toString() : null)
                .createdAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : null)
                .build();
    }

    @Override
    public boolean hasActive(UUID userId) {
        return repo.findTopByUserIdAndStatusOrderByExpiryDateDesc(userId, "ACTIVE")
                .map(s -> s.isSubscriptionActive()).orElse(false);
    }

    @Override
    public SubscriptionDto getCurrent(UUID userId) {
        return repo.findTopByUserIdAndStatusOrderByExpiryDateDesc(userId, "ACTIVE")
                .map(this::toDto).orElse(null);
    }

    @Override
    public SubscriptionDto ensureFreeSubscription(UUID userId) {
        var existing = repo.findTopByUserIdAndStatusOrderByExpiryDateDesc(userId, "ACTIVE");
        if (existing.isPresent() && existing.get().isSubscriptionActive()) {
            return toDto(existing.get());
        }
        var sub = Subscription.builder()
                .userId(userId)
                .planCode("FREE")
                .planName("Free")
                .storageLimitBytes(limitFor("FREE"))
                .maxCompanies(maxCompaniesFor("FREE"))
                .maxDocuments(maxDocsFor("FREE"))
                .adsEnabled(true)
                .status("ACTIVE")
                .startDate(LocalDateTime.now())
                .expiryDate(null)
                .build();
        var saved = repo.save(sub);
        try { quotaService.getOrCreate(userId); } catch (Exception ignored) {}
        return toDto(saved);
    }

    @Override
    public SubscriptionDto createOrder(UUID userId, String planName) {
        String code = planName != null ? planName.toUpperCase().replaceAll(" ", "_") : "FREE";
        if (!code.startsWith("PRO_") && !"FREE".equals(code)) code = "PRO_MONTHLY";
        long amount = "PRO_YEARLY".equals(code) ? 99900 : 9900;
        String orderId = razorPayService.createOrder(amount, "INR", "receipt_" + userId.toString().substring(0,8));
        var sub = Subscription.builder()
                .userId(userId)
                .planCode(code)
                .planName(planName)
                .storageLimitBytes(limitFor(code))
                .maxCompanies(maxCompaniesFor(code))
                .maxDocuments(maxDocsFor(code))
                .adsEnabled(!"FREE".equals(code) ? false : true)
                .status("PENDING")
                .razorpayOrderId(orderId)
                .startDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusDays("PRO_YEARLY".equals(code) ? 365 : 30))
                .build();
        return toDto(repo.save(sub));
    }

    @Override
    public SubscriptionDto verifyAndActivate(UUID userId, String orderId, String paymentId, String signature, String planName) {
        boolean ok = razorPayService.verifySignature(orderId + "|" + paymentId, signature);
        if (!ok) throw new RuntimeException("Invalid RazorPay signature");
        String code = planName != null ? planName.toUpperCase().replaceAll(" ", "_") : "FREE";
        if (!code.startsWith("PRO_") && !"FREE".equals(code)) code = "PRO_MONTHLY";
        var sub = repo.findTopByUserIdAndStatusOrderByExpiryDateDesc(userId, "PENDING")
                .orElse(Subscription.builder().userId(userId).planCode(code).planName(planName).status("PENDING").build());
        sub.setPlanCode(code);
        sub.setPlanName(planName);
        sub.setStorageLimitBytes(limitFor(code));
        sub.setMaxCompanies(maxCompaniesFor(code));
        sub.setMaxDocuments(maxDocsFor(code));
        sub.setAdsEnabled(!"FREE".equals(code) ? false : true);
        sub.setStatus("ACTIVE");
        sub.setRazorpayOrderId(orderId);
        sub.setRazorpayPaymentId(paymentId);
        sub.setRazorpaySignature(signature);
        sub.setStartDate(LocalDateTime.now());
        sub.setExpiryDate(LocalDateTime.now().plusDays("PRO_YEARLY".equals(code) ? 365 : 30));
        var saved = repo.save(sub);
        try { quotaService.getOrCreate(userId); } catch (Exception ignored) {}
        return toDto(saved);
    }
}
