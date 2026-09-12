package com.officebuddy.admin;

import com.officebuddy.adAndSubscription.invoice.InvoiceRepository;
import com.officebuddy.adAndSubscription.subscription.repository.SubscriptionRepository;
import com.officebuddy.company.CompanyRepository;
import com.officebuddy.document.DocumentRepository;
import com.officebuddy.reminder.repository.ReminderRepository;
import com.officebuddy.storage.quota.repository.UserStorageRepository;
import com.officebuddy.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final DocumentRepository documentRepository;
    private final CompanyRepository companyRepository;
    private final ReminderRepository reminderRepository;
    private final UserStorageRepository storageRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> stats() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("usersTotal", userRepository.count());
        out.put("admins", userRepository.countByAccessRole("admin"));
        out.put("members", userRepository.countByAccessRole("member"));
        out.put("newUsers30d", userRepository.countCreatedSince(
                Date.from(java.time.Instant.now().minusSeconds(30L * 24 * 60 * 60))));
        List<Map<String, Object>> byMonth = new ArrayList<>();
        for (Object[] row : userRepository.countByMonth()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", row[0]);
            m.put("count", row[1]);
            byMonth.add(m);
        }
        out.put("usersByMonth", byMonth.stream().skip(Math.max(0, byMonth.size() - 6)).toList());
        List<Map<String, Object>> byPlan = new ArrayList<>();
        for (Object[] row : subscriptionRepository.countActiveByPlan()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("plan", row[0]);
            m.put("count", row[1]);
            byPlan.add(m);
        }
        out.put("subscriptionsByPlan", byPlan);
        out.put("revenuePaise", invoiceRepository.sumPaidRevenue());
        out.put("invoices", invoiceRepository.count());
        out.put("documents", documentRepository.count());
        out.put("companies", companyRepository.count());
        out.put("reminders", reminderRepository.count());
        out.put("storageUsedBytes", storageRepository.sumUsedBytes());
        out.put("storageAllocatedBytes", storageRepository.sumAllocatedBytes());
        out.put("lockedAccounts", userRepository.countByAccountLockedUntilAfter(LocalDateTime.now()));
        return ResponseEntity.ok(out);
    }
}
