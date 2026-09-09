package com.officebuddy.adAndSubscription.subscription.service;

import com.officebuddy.adAndSubscription.subscription.dto.SubscriptionDto;
import java.util.UUID;

public interface SubscriptionService {
    boolean hasActive(UUID userId);
    SubscriptionDto getCurrent(UUID userId);
    SubscriptionDto ensureFreeSubscription(UUID userId);
    SubscriptionDto createOrder(UUID userId, String planName);
    SubscriptionDto verifyAndActivate(UUID userId, String orderId, String paymentId, String signature, String planName);
}
