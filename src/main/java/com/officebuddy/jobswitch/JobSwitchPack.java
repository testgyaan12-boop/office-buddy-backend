package com.officebuddy.jobswitch;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_switch_packs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSwitchPack {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private String status;

    private String bundleKey;

    private LocalDateTime generatedAt;

    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
        expiresAt = generatedAt.plusDays(7);
    }
}
