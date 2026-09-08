package com.officebuddy.adAndSubscription.provider.dto;

import lombok.Data;

@Data
public class AdProviderReq {
    private String providerName;
    private String platform;
    private Boolean isActive;
    private Integer priority;
}
