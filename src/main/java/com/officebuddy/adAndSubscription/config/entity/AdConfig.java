package com.officebuddy.adAndSubscription.config.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ad_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "ad_type", nullable = false)
    private String adType;

    @Column(nullable = false)
    private String placement;

    @Column(name = "ad_unit_id", nullable = false)
    private String adUnitId;

    @Column(name = "app_id")
    private String appId;

    @Column(nullable = false)
    private Integer priority;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
