package com.officebuddy.jobswitch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobSwitchRepository extends JpaRepository<JobSwitchPack, UUID> {
    Optional<JobSwitchPack> findTopByUserIdOrderByGeneratedAtDesc(UUID userId);
}
