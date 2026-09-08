package com.officebuddy.adAndSubscription.subscription.service.impl;

import com.officebuddy.adAndSubscription.subscription.dto.SubscriptionDto;
import com.officebuddy.adAndSubscription.subscription.entity.Subscription;
import com.officebuddy.adAndSubscription.subscription.repository.SubscriptionRepository;
import com.officebuddy.adAndSubscription.subscription.service.RazorPayService;
import com.officebuddy.adAndSubscription.subscription.service.SubscriptionService;
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

    @Value("${razorpay.key-id:}")
    private String keyId;

    private SubscriptionDto toDto(Subscription s) {
        return SubscriptionDto.builder()
                .id(s.getId().toString())
                .userId(s.getUserId().toString())
                .planName(s.getPlanName())
                .status(s.getStatus())
                .startDate(s.getStartDate() != null ? s.getStartDate().toString() : null)
                .expiryDate(s.getExpiryDate() != null ? s.getExpiryDate().toString() : null)
                .createdAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : null)
                .build();
    }

    @Override
    public boolean hasActive(UUID userId) {
        return repo.findTopByUserIdAndStatusOrderByExpiryDateDesc(userId, "ACTIVE")
                .map(s -> s.isActive()).orElse(false);
    }

    @Override
    public SubscriptionDto getCurrent(UUID userId) {
        return repo.findTopByUserIdAndStatusOrderByExpiryDateDesc(userId, "ACTIVE")
                .map(this::toDto).orElse(null);
    }

    @Override
    public SubscriptionDto createOrder(UUID userId, String planName) {
        // For MVP, create pending subscription with RazorPay order
        long amount = "PRO_YEARLY".equals(planName) ? 99900 : 9900; // paise
        String orderId = razorPayService.createOrder(amount, "INR", "receipt_" + userId.toString().substring(0,8));
        var sub = Subscription.builder()
                .userId(userId)
                .planName(planName)
                .status("PENDING")
                .razorpayOrderId(orderId)
                .startDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusDays("PRO_YEARLY".equals(planName) ? 365 : 30))
                .build();
        return toDto(repo.save(sub));
    }

    @Override
    public SubscriptionDto verifyAndActivate(UUID userId, String orderId, String paymentId, String signature, String planName) {
        boolean ok = razorPayService.verifySignature(orderId + "|" + paymentId, signature);
        if (!ok) throw new RuntimeException("Invalid RazorPay signature");
        var sub = repo.findTopByUserIdAndStatusOrderByExpiryDateDesc(userId, "PENDING")
                .orElse(Subscription.builder().userId(userId).planName(planName).status("PENDING").build());
        sub.setStatus("ACTIVE");
        sub.setRazorpayOrderId(orderId);
        sub.setRazorpayPaymentId(paymentId);
        sub.setRazorpaySignature(signature);
        sub.setStartDate(LocalDateTime.now());
        sub.setExpiryDate(LocalDateTime.now().plusDays("PRO_YEARLY".equals(planName) ? 365 : 30));
        return toDto(repo.save(sub));
    }
}
