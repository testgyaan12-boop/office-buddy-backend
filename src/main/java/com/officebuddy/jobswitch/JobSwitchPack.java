package com.officebuddy.jobswitch;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_switch_packs")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class JobSwitchPack extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private String status;

    private String bundleKey;

    private String selectedTypes; // JSON e.g. {"OFFER_LETTER":2,"PAYSLIP":3}

    private Integer downloadCount;

    private Boolean isPaid;

    private Boolean active;

    private LocalDateTime deletedAt;

    private LocalDateTime generatedAt;

    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        generatedAt = now;
        expiresAt = now.plusDays(7);
        if (active == null) active = true;
        if (downloadCount == null) downloadCount = 0;
        if (isPaid == null) isPaid = false;
    }
}
