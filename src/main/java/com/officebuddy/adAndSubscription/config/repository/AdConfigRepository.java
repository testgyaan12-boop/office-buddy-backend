package com.officebuddy.adAndSubscription.config.repository;

import com.officebuddy.adAndSubscription.config.entity.AdConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdConfigRepository extends JpaRepository<AdConfig, Long> {
    List<AdConfig> findByProviderIdAndPlacementAndAdTypeAndIsActiveOrderByPriorityAsc(Long providerId, String placement, String adType, Integer isActive);
    List<AdConfig> findByPlacementAndAdTypeAndIsActiveOrderByPriorityAsc(String placement, String adType, Integer isActive);
}
