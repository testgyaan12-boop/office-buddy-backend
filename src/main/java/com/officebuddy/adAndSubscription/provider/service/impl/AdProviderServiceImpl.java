package com.officebuddy.adAndSubscription.provider.service.impl;

import com.officebuddy.adAndSubscription.provider.dto.AdProviderDto;
import com.officebuddy.adAndSubscription.provider.dto.AdProviderReq;
import com.officebuddy.adAndSubscription.provider.entity.AdProvider;
import com.officebuddy.adAndSubscription.provider.repository.AdProviderRepository;
import com.officebuddy.adAndSubscription.provider.service.AdProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdProviderServiceImpl implements AdProviderService {

    private final AdProviderRepository repo;

    private AdProviderDto toDto(AdProvider e) {
        return AdProviderDto.builder()
                .id(e.getId())
                .providerName(e.getProviderName())
                .platform(e.getPlatform())
                .isActive(e.getIsActive())
                .priority(e.getPriority())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().toString() : null)
                .updatedAt(e.getUpdatedAt() != null ? e.getUpdatedAt().toString() : null)
                .build();
    }

    @Override
    public AdProviderDto create(AdProviderReq req) {
        var e = AdProvider.builder()
                .providerName(req.getProviderName())
                .platform(req.getPlatform())
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .priority(req.getPriority())
                .build();
        return toDto(repo.save(e));
    }

    @Override
    public AdProviderDto update(Long id, AdProviderReq req) {
        var e = repo.findById(id).orElseThrow(() -> new RuntimeException("Provider not found"));
        if (req.getProviderName() != null) e.setProviderName(req.getProviderName());
        if (req.getPlatform() != null) e.setPlatform(req.getPlatform());
        if (req.getIsActive() != null) e.setIsActive(req.getIsActive());
        if (req.getPriority() != null) e.setPriority(req.getPriority());
        return toDto(repo.save(e));
    }

    @Override
    public List<AdProviderDto> list(String platform) {
        List<AdProvider> list = platform != null ? repo.findByPlatformAndIsActiveTrueOrderByPriorityAsc(platform) : repo.findByIsActiveTrueOrderByPriorityAsc();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void toggleActive(Long id) {
        var e = repo.findById(id).orElseThrow(() -> new RuntimeException("Provider not found"));
        e.setIsActive(!e.getIsActive());
        repo.save(e);
    }
}
