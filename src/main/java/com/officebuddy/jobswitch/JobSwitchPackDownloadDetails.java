package com.officebuddy.jobswitch;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_switch_pack_download_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSwitchPackDownloadDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID packId;

    @Column(nullable = false)
    private Integer downloadCount;

    private Boolean active;

    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime downloadedAt;

    private String ipAddress;

    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String selectedTypesSnapshot;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        downloadedAt = now;
        if (active == null) active = true;
        if (downloadCount == null) downloadCount = 1;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
