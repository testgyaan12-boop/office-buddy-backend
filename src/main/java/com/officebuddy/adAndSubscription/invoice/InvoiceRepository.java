package com.officebuddy.adAndSubscription.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findByUserIdOrderByIssuedAtDesc(UUID userId);
    Optional<Invoice> findByRazorpayPaymentId(String razorpayPaymentId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.status = 'PAID'")
    long sumPaidRevenue();

    org.springframework.data.domain.Page<Invoice> findByUserId(UUID userId, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT i FROM Invoice i WHERE (:userId IS NULL OR i.userId = :userId) AND (:q IS NULL OR LOWER(i.invoiceNo) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')) OR LOWER(i.planName) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')) OR LOWER(i.status) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))")
    org.springframework.data.domain.Page<Invoice> searchAdmin(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("q") String q,
            org.springframework.data.domain.Pageable pageable);
}
