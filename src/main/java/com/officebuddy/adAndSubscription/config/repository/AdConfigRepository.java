package com.officebuddy.adAndSubscription.config.repository;

import com.officebuddy.adAndSubscription.config.entity.AdConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdConfigRepository extends JpaRepository<AdConfig, Long> {
    List<AdConfig> findByProviderIdAndPlacementAndAdTypeAndIsActiveTrueOrderByPriorityAsc(Long providerId, String placement, String adType);
    List<AdConfig> findByPlacementAndAdTypeAndIsActiveTrueOrderByPriorityAsc(String placement, String adType);
}
