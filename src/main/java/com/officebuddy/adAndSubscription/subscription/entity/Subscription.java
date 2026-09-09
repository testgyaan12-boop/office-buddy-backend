package com.officebuddy.adAndSubscription.subscription.entity;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "plan_code", nullable = false)
    private String planCode;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "storage_limit_bytes", nullable = false)
    private Long storageLimitBytes;

    @Column(name = "max_companies")
    private Integer maxCompanies;

    @Column(name = "max_documents")
    private Integer maxDocuments;

    @Column(name = "ads_enabled")
    @Builder.Default
    private Boolean adsEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature")
    private String razorpaySignature;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @PrePersist
    protected void onCreate() {
        if (startDate == null) startDate = LocalDateTime.now();
        if (planCode == null) planCode = planName != null ? planName.toUpperCase().replaceAll(" ", "_") : "FREE";
        if (storageLimitBytes == null) {
            if ("PRO_YEARLY".equals(planCode)) storageLimitBytes = 10737418240L;
            else if ("PRO_MONTHLY".equals(planCode)) storageLimitBytes = 5368709120L;
            else storageLimitBytes = 209715200L;
        }
        if (adsEnabled == null) adsEnabled = !"FREE".equals(planCode) ? false : true;
    }

    public boolean isSubscriptionActive() {
        if (!"ACTIVE".equals(status)) return false;
        if (expiryDate == null) return true;
        return expiryDate.isAfter(LocalDateTime.now());
    }
}
