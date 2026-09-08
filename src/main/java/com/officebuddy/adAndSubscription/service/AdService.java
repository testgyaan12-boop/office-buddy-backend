package com.officebuddy.adAndSubscription.service;

import com.officebuddy.adAndSubscription.config.dto.AdConfigRes;
import java.util.UUID;

public interface AdService {
    AdConfigRes getAdConfig(String platform, String placement, String adType, UUID userId);
    void recordLog(UUID userId, String provider, String placement, String adType, String status, String errorCode);
}
