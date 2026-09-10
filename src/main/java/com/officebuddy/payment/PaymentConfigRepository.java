package com.officebuddy.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentConfigRepository extends JpaRepository<PaymentConfig, Long> {
    Optional<PaymentConfig> findByProvider(String provider);
    List<PaymentConfig> findByIsActiveAndIsDeletedOrderByIdAsc(Integer isActive, Integer isDeleted);
}
