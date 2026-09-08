package com.officebuddy.adAndSubscription.provider.service;

import com.officebuddy.adAndSubscription.provider.dto.AdProviderDto;
import com.officebuddy.adAndSubscription.provider.dto.AdProviderReq;
import java.util.List;

public interface AdProviderService {
    AdProviderDto create(AdProviderReq req);
    AdProviderDto update(Long id, AdProviderReq req);
    List<AdProviderDto> list(String platform);
    void toggleActive(Long id);
}
