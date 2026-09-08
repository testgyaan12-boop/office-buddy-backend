package com.officebuddy.adAndSubscription.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdProviderRes {
    private Long id;
    private String providerName;
    private String platform;
    private Boolean isActive;
    private Integer priority;
}
