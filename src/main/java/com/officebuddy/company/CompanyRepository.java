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
}
