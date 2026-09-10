package com.officebuddy.adAndSubscription.subscription.service.impl;

import com.officebuddy.adAndSubscription.subscription.service.RazorPayService;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RazorPayServiceImpl implements RazorPayService {

    @Value("${razorpay.key-id:}")
    private String keyId;
    @Value("${razorpay.key-secret:}")
    private String keySecret;

    @jakarta.annotation.PostConstruct
    public void logMode() {
        boolean mock = keyId == null || keyId.isBlank() || keyId.contains("dummy");
        String hint = (keyId != null && keyId.length() > 8) ? keyId.substring(0, 8) + "..." : "none";
        log.info("Razorpay mode: {} (keyId: {})", mock ? "MOCK - no real payments possible" : "LIVE", hint);
    }

    @Override
    public String createOrder(long amountPaise, String currency, String receipt) {
        try {
            if (keyId == null || keyId.isBlank() || keyId.contains("dummy")) {
                // Mock for local dev without real keys
                return "order_mock_" + System.currentTimeMillis();
            }
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject opts = new JSONObject();
            opts.put("amount", amountPaise);
            opts.put("currency", currency);
            opts.put("receipt", receipt);
            var order = client.orders.create(opts);
            return order.get("id").toString();
        } catch (Exception e) {
            log.error("RazorPay createOrder failed", e);
            return "order_mock_" + System.currentTimeMillis();
        }
    }

    @Override
    public boolean verifySignature(String payload, String signature) {
        try {
            if (keySecret == null || keySecret.contains("dummy")) return true; // allow in dev
            return Utils.verifySignature(payload, signature, keySecret);
        } catch (Exception e) {
            log.error("verifySignature failed", e);
            return false;
        }
    }
}
