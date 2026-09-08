package com.officebuddy.adAndSubscription.config.dto;

import lombok.Data;

@Data
public class AdConfigReq {
    private Long providerId;
    private String adType;
    private String placement;
    private String adUnitId;
    private String appId;
    private Integer priority;
    private Boolean isActive;
}
