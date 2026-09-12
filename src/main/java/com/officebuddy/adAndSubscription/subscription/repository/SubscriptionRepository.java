package com.officebuddy.adAndSubscription.subscription.repository;

import com.officebuddy.adAndSubscription.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Subscription> findTopByUserIdAndStatusOrderByExpiryDateDesc(UUID userId, String status);

    @org.springframework.data.jpa.repository.Query("SELECT s.planCode, COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE' GROUP BY s.planCode")
    List<Object[]> countActiveByPlan();

    org.springframework.data.domain.Page<Subscription> findByUserId(UUID userId, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Subscription s WHERE (:userId IS NULL OR s.userId = :userId) AND (:q IS NULL OR LOWER(s.planCode) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(s.planName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(s.status) LIKE LOWER(CONCAT('%', :q, '%')))")
    org.springframework.data.domain.Page<Subscription> searchAdmin(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("q") String q,
            org.springframework.data.domain.Pageable pageable);
}
