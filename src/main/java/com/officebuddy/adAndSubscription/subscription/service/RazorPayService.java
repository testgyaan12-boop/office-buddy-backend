package com.officebuddy.adAndSubscription.subscription.service;

public interface RazorPayService {
    String createOrder(long amountPaise, String currency, String receipt);
    boolean verifySignature(String payload, String signature);
}
