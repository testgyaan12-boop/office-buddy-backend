package com.officebuddy.adAndSubscription.log.repository;

import com.officebuddy.adAndSubscription.log.entity.AdRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdRequestLogRepository extends JpaRepository<AdRequestLog, Long> {
    List<AdRequestLog> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<AdRequestLog> findByProviderAndPlacementAndAdType(String provider, String placement, String adType);
}
