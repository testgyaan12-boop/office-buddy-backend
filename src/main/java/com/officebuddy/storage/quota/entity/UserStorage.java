package com.officebuddy.storage.quota.entity;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_storage")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserStorage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "allocated_bytes", nullable = false)
    private Long allocatedBytes;

    @Column(name = "used_bytes", nullable = false)
    @Builder.Default
    private Long usedBytes = 0L;

    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;

    @PrePersist
    protected void onCreate() {
        lastCalculatedAt = LocalDateTime.now();
        if (allocatedBytes == null) allocatedBytes = 209715200L;
        if (usedBytes == null) usedBytes = 0L;
    }

    public long getRemainingBytes() {
        return Math.max(0, allocatedBytes - usedBytes);
    }

    public int getUsagePercentage() {
        if (allocatedBytes == 0) return 0;
        return (int) Math.min(100, (usedBytes * 100 / allocatedBytes));
    }
}
