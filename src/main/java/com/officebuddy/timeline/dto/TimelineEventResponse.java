package com.officebuddy.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEventResponse {
    private String id;
    private String title;
    private String description;
    private String eventType;
    private String companyId;
    private String documentId;
    private String companyName;
    private String eventDate;
    private String documentDate;
    private String uploadedAt;
}
