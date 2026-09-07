package com.officebuddy.reminder.dto;

import lombok.Data;

@Data
public class ReminderRequest {
    private String title;
    private String description;
    private String jd;
    private String type;
    private String category;
    private String companyId;
    private String fileKey;
    private String fileUrl;
    private String fileName;
    private String remindAt; // ISO 8601
    private Integer notifyBeforeMinutes;
    private String deviceId; // for history tracking
}
