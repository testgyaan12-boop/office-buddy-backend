package com.officebuddy.adAndSubscription.subscription.plan.repository;

import com.officebuddy.adAndSubscription.subscription.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByPlanCode(String planCode);
}
