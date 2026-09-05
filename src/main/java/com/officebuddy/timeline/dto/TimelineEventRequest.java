package com.officebuddy.timeline.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TimelineEventRequest {
    private UUID companyId;
    private String title;
    private String description;
    private String eventType;
    private String companyName;
    private LocalDate eventDate;
    private LocalDate documentDate;
    private LocalDateTime uploadedAt;
}
