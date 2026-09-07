package com.officebuddy.reminder.service;

import com.officebuddy.company.CompanyRepository;
import com.officebuddy.reminder.dto.ReminderHistoryDto;
import com.officebuddy.reminder.dto.ReminderRequest;
import com.officebuddy.reminder.dto.ReminderResponse;
import com.officebuddy.reminder.entity.Reminder;
import com.officebuddy.reminder.entity.ReminderHistory;
import com.officebuddy.reminder.repository.ReminderHistoryRepository;
import com.officebuddy.reminder.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final ReminderHistoryRepository historyRepository;
    private final CompanyRepository companyRepository;

    private LocalDateTime parse(String iso) {
        try {
            return LocalDateTime.parse(iso);
        } catch (Exception e) {
            return LocalDateTime.parse(iso, DateTimeFormatter.ISO_DATE_TIME);
        }
    }

    private ReminderResponse toResponse(Reminder r) {
        String companyName = null;
        if (r.getCompanyId() != null) {
            var c = companyRepository.findById(r.getCompanyId()).orElse(null);
            if (c != null) companyName = c.getName();
        }
        return ReminderResponse.builder()
                .id(r.getId().toString())
                .title(r.getTitle())
                .description(r.getDescription())
                .jd(r.getJd())
                .type(r.getType())
                .category(r.getCategory())
                .companyId(r.getCompanyId() != null ? r.getCompanyId().toString() : null)
                .companyName(companyName)
                .fileKey(r.getFileKey())
                .fileUrl(r.getFileUrl())
                .fileName(r.getFileName())
                .remindAt(r.getRemindAt() != null ? r.getRemindAt().toString() : null)
                .notifyBeforeMinutes(r.getNotifyBeforeMinutes())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : null)
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .createdBy(r.getCreatedBy() != null ? r.getCreatedBy().toString() : null)
                .updatedBy(r.getUpdatedBy() != null ? r.getUpdatedBy().toString() : null)
                .build();
    }

    private ReminderHistoryDto toHistoryDto(ReminderHistory h) {
        return ReminderHistoryDto.builder()
                .id(h.getId().toString())
                .reminderId(h.getReminderId().toString())
                .userId(h.getUserId().toString())
                .deviceId(h.getDeviceId())
                .notificationSentAt(h.getNotificationSentAt() != null ? h.getNotificationSentAt().toString() : null)
                .status(h.getStatus())
                .channel(h.getChannel())
                .createdAt(h.getCreatedAt() != null ? h.getCreatedAt().toString() : null)
                .build();
    }

    public List<ReminderResponse> getReminders(UUID userId, String category) {
        List<Reminder> list;
        if (category != null && !category.isEmpty()) {
            list = reminderRepository.findByUserIdAndCategoryAndIsDeletedFalseOrderByRemindAtAsc(userId, category);
        } else {
            list = reminderRepository.findByUserIdAndIsDeletedFalseOrderByRemindAtAsc(userId);
        }
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ReminderResponse getReminder(UUID userId, UUID id) {
        var r = reminderRepository.findById(id).orElseThrow(() -> new RuntimeException("Reminder not found"));
        if (!r.getUserId().equals(userId) || Boolean.TRUE.equals(r.getIsDeleted())) throw new RuntimeException("Not found");
        return toResponse(r);
    }

    @Transactional
    public ReminderResponse createReminder(UUID userId, ReminderRequest req) {
        var remindAt = parse(req.getRemindAt());
        UUID compId = null;
        if (req.getCompanyId() != null && !req.getCompanyId().isBlank()) {
            try { compId = UUID.fromString(req.getCompanyId()); } catch (Exception ignored) {}
        }
        var entity = Reminder.builder()
                .userId(userId)
                .title(req.getTitle())
                .description(req.getDescription())
                .jd(req.getJd())
                .type(req.getType())
                .category(req.getCategory() != null ? req.getCategory().toLowerCase() : "personal")
                .companyId(compId)
                .fileKey(req.getFileKey())
                .fileUrl(req.getFileUrl())
                .fileName(req.getFileName())
                .remindAt(remindAt)
                .notifyBeforeMinutes(req.getNotifyBeforeMinutes() != null ? req.getNotifyBeforeMinutes() : 60)
                .isDeleted(false)
                .createdBy(userId)
                .updatedBy(userId)
                .build();
        reminderRepository.save(entity);
        // audit history for creation (optional) — log initial schedule
        if (req.getDeviceId() != null) {
            var h = ReminderHistory.builder()
                    .reminderId(entity.getId())
                    .userId(userId)
                    .deviceId(req.getDeviceId())
                    .notificationSentAt(LocalDateTime.now())
                    .status("SCHEDULED")
                    .channel("local")
                    .build();
            historyRepository.save(h);
        }
        return toResponse(entity);
    }

    @Transactional
    public ReminderResponse updateReminder(UUID userId, UUID id, ReminderRequest req) {
        var r = reminderRepository.findById(id).orElseThrow(() -> new RuntimeException("Reminder not found"));
        if (!r.getUserId().equals(userId) || Boolean.TRUE.equals(r.getIsDeleted())) throw new RuntimeException("Not found");
        if (req.getTitle() != null) r.setTitle(req.getTitle());
        if (req.getDescription() != null) r.setDescription(req.getDescription());
        if (req.getJd() != null) r.setJd(req.getJd());
        if (req.getType() != null) r.setType(req.getType());
        if (req.getCategory() != null) r.setCategory(req.getCategory().toLowerCase());
        if (req.getCompanyId() != null) {
            try { r.setCompanyId(req.getCompanyId().isBlank() ? null : UUID.fromString(req.getCompanyId())); } catch (Exception ignored) {}
        }
        if (req.getFileKey() != null) r.setFileKey(req.getFileKey());
        if (req.getFileUrl() != null) r.setFileUrl(req.getFileUrl());
        if (req.getFileName() != null) r.setFileName(req.getFileName());
        if (req.getRemindAt() != null) r.setRemindAt(parse(req.getRemindAt()));
        if (req.getNotifyBeforeMinutes() != null) r.setNotifyBeforeMinutes(req.getNotifyBeforeMinutes());
        r.setUpdatedBy(userId);
        reminderRepository.save(r);
        return toResponse(r);
    }

    @Transactional
    public void deleteReminder(UUID userId, UUID id) {
        var r = reminderRepository.findById(id).orElseThrow(() -> new RuntimeException("Reminder not found"));
        if (!r.getUserId().equals(userId)) throw new RuntimeException("Unauthorized");
        r.setIsDeleted(true);
        r.setDeletedAt(LocalDateTime.now());
        r.setUpdatedBy(userId);
        reminderRepository.save(r);
    }

    @Transactional
    public ReminderHistoryDto logNotification(UUID userId, UUID reminderId, String deviceId, String status) {
        var r = reminderRepository.findById(reminderId).orElseThrow(() -> new RuntimeException("Reminder not found"));
        if (!r.getUserId().equals(userId)) throw new RuntimeException("Unauthorized");
        var h = ReminderHistory.builder()
                .reminderId(reminderId)
                .userId(userId)
                .deviceId(deviceId)
                .notificationSentAt(LocalDateTime.now())
                .status(status != null ? status : "SENT")
                .channel("local")
                .build();
        historyRepository.save(h);
        return toHistoryDto(h);
    }

    public List<ReminderHistoryDto> getHistory(UUID userId, UUID reminderId) {
        List<ReminderHistory> list;
        if (reminderId != null) {
            list = historyRepository.findByUserIdAndReminderIdOrderByNotificationSentAtDesc(userId, reminderId);
        } else {
            list = historyRepository.findByUserIdOrderByNotificationSentAtDesc(userId);
        }
        return list.stream().map(this::toHistoryDto).collect(Collectors.toList());
    }
}
