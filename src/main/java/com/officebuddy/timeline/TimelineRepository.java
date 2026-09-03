package com.officebuddy.timeline;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimelineRepository extends JpaRepository<TimelineEvent, UUID> {
    @org.springframework.data.jpa.repository.Query("SELECT t FROM TimelineEvent t WHERE t.userId = ?1 AND t.deletedAt IS NULL ORDER BY t.eventDate DESC")
    List<TimelineEvent> findByUserIdOrderByEventDateDesc(UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TimelineEvent t WHERE t.companyId = ?1 AND t.deletedAt IS NULL ORDER BY t.eventDate DESC")
    List<TimelineEvent> findByCompanyIdOrderByEventDateDesc(UUID companyId);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TimelineEvent t WHERE t.companyId = ?1 AND t.userId = ?2 AND t.deletedAt IS NULL ORDER BY t.eventDate DESC")
    List<TimelineEvent> findByCompanyIdAndUserIdOrderByEventDateDesc(UUID companyId, UUID userId);
}
