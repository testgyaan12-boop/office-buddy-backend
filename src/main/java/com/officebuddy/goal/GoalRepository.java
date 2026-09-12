package com.officebuddy.goal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {

    @Query("SELECT g FROM Goal g WHERE g.userId = ?1 AND g.deletedAt IS NULL ORDER BY g.targetDate ASC")
    List<Goal> findByUserIdAndDeletedAtIsNull(UUID userId);

    @Query("SELECT g FROM Goal g WHERE g.userId = ?1 AND g.deletedAt IS NULL AND g.status = 'active' ORDER BY g.targetDate ASC")
    List<Goal> findActiveByUserId(UUID userId);

    org.springframework.data.domain.Page<Goal> findByUserId(UUID userId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT g FROM Goal g WHERE (:userId IS NULL OR g.userId = :userId) AND (:q IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(g.category) LIKE LOWER(CONCAT('%', :q, '%')))")
    org.springframework.data.domain.Page<Goal> searchAdmin(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("q") String q,
            org.springframework.data.domain.Pageable pageable);
}
