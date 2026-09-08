package com.officebuddy.adAndSubscription.subscription.controller;

import com.officebuddy.adAndSubscription.subscription.dto.SubscriptionDto;
import com.officebuddy.adAndSubscription.subscription.service.SubscriptionService;
import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService service;

    @GetMapping("/me")
    public ResponseEntity<SubscriptionDto> me(Authentication auth) {
        var user = (User) auth.getPrincipal();
        var dto = service.getCurrent(user.getId());
        if (dto == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/create-order")
    public ResponseEntity<SubscriptionDto> createOrder(Authentication auth, @RequestBody Map<String, String> body) {
        var user = (User) auth.getPrincipal();
        String plan = body.getOrDefault("planName", "PRO_MONTHLY");
        return ResponseEntity.ok(service.createOrder(user.getId(), plan));
    }

    @PostMapping("/verify")
    public ResponseEntity<SubscriptionDto> verify(Authentication auth, @RequestBody Map<String, String> body) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(service.verifyAndActivate(
                user.getId(),
                body.get("razorpay_order_id"),
                body.get("razorpay_payment_id"),
                body.get("razorpay_signature"),
                body.getOrDefault("planName", "PRO_MONTHLY")));
    }
}
