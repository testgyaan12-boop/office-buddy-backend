package com.officebuddy.reminder.entity;

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
@Table(name = "reminder_history")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reminder_id", nullable = false)
    private UUID reminderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "notification_sent_at", nullable = false)
    private LocalDateTime notificationSentAt;

    @Column
    @Builder.Default
    private String status = "SENT";

    @Column
    @Builder.Default
    private String channel = "local";

    @PrePersist
    protected void onCreate() {
        if (notificationSentAt == null) notificationSentAt = LocalDateTime.now();
        if (status == null) status = "SENT";
        if (channel == null) channel = "local";
    }
}
