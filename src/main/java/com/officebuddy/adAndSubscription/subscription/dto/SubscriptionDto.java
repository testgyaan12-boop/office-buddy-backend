package com.officebuddy.adAndSubscription.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {
    private String id;
    private String userId;
    private String planCode;
    private String planName;
    private Long storageLimitBytes;
    private Integer maxCompanies;
    private Integer maxDocuments;
    private Boolean adsEnabled;
    private String status;
    private String startDate;
    private String expiryDate;
    private String createdAt;
    private String razorpayOrderId;
    private Long amountPaise;
    private String currency;
    private String razorpayKeyId;
}
