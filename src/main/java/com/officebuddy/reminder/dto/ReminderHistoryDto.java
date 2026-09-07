package com.officebuddy.reminder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderHistoryDto {
    private String id;
    private String reminderId;
    private String userId;
    private String deviceId;
    private String notificationSentAt;
    private String status;
    private String channel;
    private String createdAt;
}
