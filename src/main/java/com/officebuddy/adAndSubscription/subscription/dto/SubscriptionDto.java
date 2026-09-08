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
    private String planName;
    private String status;
    private String startDate;
    private String expiryDate;
    private String createdAt;
}
