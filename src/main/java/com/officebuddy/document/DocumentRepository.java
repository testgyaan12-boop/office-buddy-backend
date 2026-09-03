package com.officebuddy.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    @Query("SELECT d FROM Document d WHERE d.userId = ?1 AND d.deletedAt IS NULL ORDER BY d.uploadedAt DESC")
    List<Document> findByUserIdOrderByUploadedAtDesc(UUID userId);

    @Query("SELECT d FROM Document d WHERE d.companyId = ?1 AND d.userId = ?2 AND d.deletedAt IS NULL ORDER BY d.uploadedAt DESC")
    List<Document> findByCompanyIdAndUserIdOrderByUploadedAtDesc(UUID companyId, UUID userId);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.userId = ?1 AND d.deletedAt IS NULL")
    long countByUserId(UUID userId);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.companyId = ?1 AND d.deletedAt IS NULL")
    long countByCompanyId(UUID companyId);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.userId = ?1 AND d.type = 'CERTIFICATE' AND d.deletedAt IS NULL")
    long countByUserIdAndType(UUID userId);

    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND d.deletedAt IS NULL AND " +
           "(:query IS NULL OR :query = '' OR LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.fileName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "EXISTS (SELECT c FROM com.officebuddy.company.Company c WHERE c.id = d.companyId AND c.deletedAt IS NULL AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')))) AND " +
           "(:type IS NULL OR d.type = :type) ORDER BY d.uploadedAt DESC")
    List<Document> search(UUID userId, String query, DocumentType type);
}
