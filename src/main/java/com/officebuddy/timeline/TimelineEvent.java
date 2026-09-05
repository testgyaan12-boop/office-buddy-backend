package com.officebuddy.timeline;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "timeline_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private UUID companyId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String eventType;

    private String companyName;

    @Column(nullable = false)
    private LocalDate eventDate;

    private LocalDate documentDate;

    private LocalDateTime uploadedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

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
