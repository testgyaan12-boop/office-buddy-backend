package com.officebuddy.adAndSubscription.log.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdRequestLogRes {
    private Long id;
    private String userId;
    private String provider;
    private String placement;
    private String adType;
    private String status;
    private String createdAt;
}
