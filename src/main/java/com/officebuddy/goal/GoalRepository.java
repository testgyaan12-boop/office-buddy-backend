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
}
