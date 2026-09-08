package com.officebuddy.adAndSubscription.log.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ad_request_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String placement;

    @Column(name = "ad_type", nullable = false)
    private String adType;

    @Column(nullable = false)
    @Builder.Default
    private String status = "SHOWN";

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
