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
}
