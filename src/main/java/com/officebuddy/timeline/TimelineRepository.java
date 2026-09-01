package com.officebuddy.timeline;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimelineRepository extends JpaRepository<TimelineEvent, UUID> {
    List<TimelineEvent> findByUserIdOrderByEventDateDesc(UUID userId);
    List<TimelineEvent> findByCompanyIdOrderByEventDateDesc(UUID companyId);
    List<TimelineEvent> findByCompanyIdAndUserIdOrderByEventDateDesc(UUID companyId, UUID userId);
}
