package com.officebuddy.jobswitch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobSwitchPackDownloadDetailsRepository extends JpaRepository<JobSwitchPackDownloadDetails, UUID> {
    List<JobSwitchPackDownloadDetails> findByUserIdOrderByDownloadedAtDesc(UUID userId);
    List<JobSwitchPackDownloadDetails> findByPackIdOrderByDownloadedAtDesc(UUID packId);
    long countByUserIdAndDeletedAtIsNull(UUID userId);
    long countByPackIdAndDeletedAtIsNull(UUID packId);
    List<JobSwitchPackDownloadDetails> findByDeletedAtIsNullOrderByDownloadedAtDesc();
}
