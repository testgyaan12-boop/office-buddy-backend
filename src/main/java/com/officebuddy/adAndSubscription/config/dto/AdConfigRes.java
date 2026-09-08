package com.officebuddy.adAndSubscription.config.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdConfigRes {
    private Long id;
    private Long providerId;
    private String providerName;
    private String platform;
    private String adType;
    private String placement;
    private String adUnitId;
    private String appId;
    private Integer priority;
    private Boolean isActive;
    private String fallbackProvider;
    private String fallbackAdUnitId;
}
