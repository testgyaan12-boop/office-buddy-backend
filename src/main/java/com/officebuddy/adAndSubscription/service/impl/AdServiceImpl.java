package com.officebuddy.adAndSubscription.service.impl;

import com.officebuddy.adAndSubscription.config.dto.AdConfigRes;
import com.officebuddy.adAndSubscription.config.entity.AdConfig;
import com.officebuddy.adAndSubscription.config.repository.AdConfigRepository;
import com.officebuddy.adAndSubscription.log.entity.AdRequestLog;
import com.officebuddy.adAndSubscription.log.repository.AdRequestLogRepository;
import com.officebuddy.adAndSubscription.provider.entity.AdProvider;
import com.officebuddy.adAndSubscription.provider.repository.AdProviderRepository;
import com.officebuddy.adAndSubscription.service.AdService;
import com.officebuddy.adAndSubscription.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdServiceImpl implements AdService {

    private final AdProviderRepository providerRepo;
    private final AdConfigRepository configRepo;
    private final AdRequestLogRepository logRepo;
    private final SubscriptionService subscriptionService;

    @Override
    public AdConfigRes getAdConfig(String platform, String placement, String adType, UUID userId) {
        if (subscriptionService.hasActive(userId)) return null;
        List<AdProvider> providers = providerRepo.findByPlatformAndIsActiveTrueOrderByPriorityAsc(platform);
        AdConfig primary = null;
        AdConfig fallback = null;
        for (AdProvider p : providers) {
            var configs = configRepo.findByProviderIdAndPlacementAndAdTypeAndIsActiveTrueOrderByPriorityAsc(p.getId(), placement, adType);
            if (!configs.isEmpty()) {
                if (primary == null) primary = configs.get(0);
                else if (fallback == null) { fallback = configs.get(0); break; }
            }
        }
        if (primary == null) return null;
        var primaryProvider = providerRepo.findById(primary.getProviderId()).orElse(null);
        String fallbackProvider = null;
        String fallbackUnit = null;
        if (fallback != null) {
            var fp = providerRepo.findById(fallback.getProviderId()).orElse(null);
            fallbackProvider = fp != null ? fp.getProviderName() : null;
            fallbackUnit = fallback.getAdUnitId();
        }
        return AdConfigRes.builder()
                .id(primary.getId())
                .providerId(primary.getProviderId())
                .providerName(primaryProvider != null ? primaryProvider.getProviderName() : null)
                .platform(primaryProvider != null ? primaryProvider.getPlatform() : platform)
                .adType(primary.getAdType())
                .placement(primary.getPlacement())
                .adUnitId(primary.getAdUnitId())
                .appId(primary.getAppId())
                .priority(primary.getPriority())
                .isActive(primary.getIsActive())
                .fallbackProvider(fallbackProvider)
                .fallbackAdUnitId(fallbackUnit)
                .build();
    }

    @Override
    public void recordLog(UUID userId, String provider, String placement, String adType, String status, String errorCode) {
        var log = AdRequestLog.builder()
                .userId(userId)
                .provider(provider)
                .placement(placement)
                .adType(adType)
                .status(status)
                .errorCode(errorCode)
                .build();
        logRepo.save(log);
    }
}
