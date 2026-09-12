package com.officebuddy.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    @Query("SELECT c FROM Company c WHERE c.userId = ?1 AND c.deletedAt IS NULL ORDER BY c.startDate DESC")
    List<Company> findByUserIdOrderByStartDateDesc(UUID userId);

    @Query("SELECT COUNT(c) FROM Company c WHERE c.userId = ?1 AND c.deletedAt IS NULL")
    long countByUserId(UUID userId);

    org.springframework.data.domain.Page<Company> findByUserId(UUID userId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT c FROM Company c WHERE (:userId IS NULL OR c.userId = :userId) AND (:q IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.role) LIKE LOWER(CONCAT('%', :q, '%'))) AND (:from IS NULL OR c.startDate >= :from) AND (:to IS NULL OR c.startDate <= :to)")
    org.springframework.data.domain.Page<Company> searchAdmin(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("q") String q,
            @org.springframework.data.repository.query.Param("from") java.time.LocalDate from,
            @org.springframework.data.repository.query.Param("to") java.time.LocalDate to,
            org.springframework.data.domain.Pageable pageable);
}
