package com.officebuddy.reminder.entity;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reminders")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Reminder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String jd;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String category;

    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "file_key")
    private String fileKey;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    @Column(name = "notify_before_minutes", nullable = false)
    private Integer notifyBeforeMinutes;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
