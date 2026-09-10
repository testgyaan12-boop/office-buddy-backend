package com.officebuddy.adAndSubscription.subscription.service.impl;

import com.officebuddy.adAndSubscription.subscription.service.RazorPayService;
import com.officebuddy.payment.PaymentConfigService;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorPayServiceImpl implements RazorPayService {

    private final PaymentConfigService paymentConfigService;

    @Value("${razorpay.key-id:}")
    private String keyId;
    @Value("${razorpay.key-secret:}")
    private String keySecret;

    private String[] credentials() {
        try {
            var cfg = paymentConfigService.usableProvider("razorpay").orElse(null);
            if (cfg != null) return new String[]{cfg.getKeyId(), cfg.getSecretKey()};
        } catch (Exception ignored) {}
        return new String[]{keyId, keySecret};
    }

    @jakarta.annotation.PostConstruct
    public void logMode() {
        try {
            String[] creds = credentials();
            String id = creds[0];
            boolean mock = id == null || id.isBlank() || id.contains("dummy");
            String hint = (id != null && id.length() > 8) ? id.substring(0, 8) + "..." : "none";
            log.info("Razorpay mode: {} (keyId: {})", mock ? "MOCK - no real payments possible" : "LIVE", hint);
        } catch (Exception e) {
            log.warn("Razorpay mode check skipped: {}", e.getMessage());
        }
    }

    @Override
    public String createOrder(long amountPaise, String currency, String receipt) {
        try {
            String[] creds = credentials();
            String id = creds[0];
            String secret = creds[1];
            if (id == null || id.isBlank() || id.contains("dummy")) {
                // Mock for local dev without real keys
                return "order_mock_" + System.currentTimeMillis();
            }
            RazorpayClient client = new RazorpayClient(id, secret);
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
            String secret = credentials()[1];
            if (secret == null || secret.contains("dummy")) return true; // allow in dev
            return Utils.verifySignature(payload, signature, secret);
        } catch (Exception e) {
            log.error("verifySignature failed", e);
            return false;
        }
    }
}
