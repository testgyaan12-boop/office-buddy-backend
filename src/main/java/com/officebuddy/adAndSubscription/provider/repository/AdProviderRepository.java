package com.officebuddy.adAndSubscription.provider.repository;

import com.officebuddy.adAndSubscription.provider.entity.AdProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdProviderRepository extends JpaRepository<AdProvider, Long> {
    List<AdProvider> findByPlatformAndIsActiveTrueOrderByPriorityAsc(String platform);
    List<AdProvider> findByIsActiveTrueOrderByPriorityAsc();
}
