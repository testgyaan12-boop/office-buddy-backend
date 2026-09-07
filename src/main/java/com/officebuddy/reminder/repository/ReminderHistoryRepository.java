package com.officebuddy.reminder.repository;

import com.officebuddy.reminder.entity.ReminderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderHistoryRepository extends JpaRepository<ReminderHistory, UUID> {
    List<ReminderHistory> findByUserIdOrderByNotificationSentAtDesc(UUID userId);
    List<ReminderHistory> findByReminderIdOrderByNotificationSentAtDesc(UUID reminderId);
    List<ReminderHistory> findByUserIdAndReminderIdOrderByNotificationSentAtDesc(UUID userId, UUID reminderId);
}
