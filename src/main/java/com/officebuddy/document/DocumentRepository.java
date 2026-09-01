package com.officebuddy.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByUserIdOrderByUploadedAtDesc(UUID userId);
    List<Document> findByCompanyIdAndUserIdOrderByUploadedAtDesc(UUID companyId, UUID userId);
    long countByUserId(UUID userId);
    long countByCompanyId(UUID companyId);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.userId = ?1 AND d.type = 'CERTIFICATE'")
    long countByUserIdAndType(UUID userId);

    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND " +
           "(:query IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.fileName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:type IS NULL OR d.type = :type)")
    List<Document> search(UUID userId, String query, DocumentType type);
}
