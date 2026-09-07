package com.officebuddy.reminder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderResponse {
    private String id;
    private String title;
    private String description;
    private String jd;
    private String type;
    private String category;
    private String companyId;
    private String companyName;
    private String fileKey;
    private String fileUrl;
    private String fileName;
    private String remindAt;
    private Integer notifyBeforeMinutes;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String updatedBy;
}
