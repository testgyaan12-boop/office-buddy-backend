package com.officebuddy.todo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TodoRepository extends JpaRepository<Todo, UUID> {

    @Query("SELECT t FROM Todo t WHERE t.userId = ?1 AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Todo> findByUserId(UUID userId);

    @Query("SELECT t FROM Todo t WHERE t.userId = ?1 AND t.deletedAt IS NULL AND t.type = ?2 ORDER BY t.createdAt DESC")
    List<Todo> findByUserIdAndType(UUID userId, String type);

    @Query("SELECT t FROM Todo t WHERE t.userId = ?1 AND t.deletedAt IS NULL AND LOWER(t.title) LIKE LOWER(CONCAT('%', ?2, '%')) ORDER BY t.createdAt DESC")
    List<Todo> findByUserIdAndTitleContainingIgnoreCase(UUID userId, String search);

    @Query("SELECT t FROM Todo t WHERE t.userId = ?1 AND t.deletedAt IS NULL AND t.dueDate = ?2 ORDER BY t.createdAt DESC")
    List<Todo> findByUserIdAndDueDate(UUID userId, LocalDate date);

    @Query("SELECT t FROM Todo t WHERE t.userId = ?1 AND t.deletedAt IS NULL AND t.goalId = ?2 ORDER BY t.createdAt DESC")
    List<Todo> findByUserIdAndGoalId(UUID userId, UUID goalId);

    org.springframework.data.domain.Page<Todo> findByUserId(UUID userId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT t FROM Todo t WHERE (:userId IS NULL OR t.userId = :userId) AND (:type IS NULL OR t.type = :type) AND (:q IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))")
    org.springframework.data.domain.Page<Todo> searchAdmin(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("type") String type,
            @org.springframework.data.repository.query.Param("q") String q,
            org.springframework.data.domain.Pageable pageable);
}
