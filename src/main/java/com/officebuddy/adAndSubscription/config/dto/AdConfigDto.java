package com.officebuddy.adAndSubscription.config.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdConfigDto {
    private Long id;
    private Long providerId;
    private String adType;
    private String placement;
    private String adUnitId;
    private String appId;
    private Integer priority;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
}
