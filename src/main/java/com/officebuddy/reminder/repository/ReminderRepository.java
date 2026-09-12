package com.officebuddy.reminder.repository;

import com.officebuddy.reminder.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID> {
    List<Reminder> findByUserIdAndIsDeletedOrderByRemindAtAsc(UUID userId, Integer isDeleted);
    List<Reminder> findByUserIdAndCategoryAndIsDeletedOrderByRemindAtAsc(UUID userId, String category, Integer isDeleted);

    org.springframework.data.domain.Page<Reminder> findByUserId(UUID userId, org.springframework.data.domain.Pageable pageable);
}
