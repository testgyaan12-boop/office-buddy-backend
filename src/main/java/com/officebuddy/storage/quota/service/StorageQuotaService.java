package com.officebuddy.storage.quota.service;

import com.officebuddy.document.DocumentRepository;
import com.officebuddy.storage.quota.dto.StorageQuotaDto;
import com.officebuddy.storage.quota.entity.UserStorage;
import com.officebuddy.storage.quota.repository.UserStorageRepository;
import com.officebuddy.adAndSubscription.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageQuotaService {

    private final UserStorageRepository storageRepo;
    private final DocumentRepository documentRepo;
    private final SubscriptionRepository subscriptionRepo;

    private long limitForPlan(String planCode) {
        if (planCode == null) return 209715200L;
        switch (planCode) {
            case "PRO_MONTHLY": return 5368709120L;
            case "PRO_YEARLY": return 10737418240L;
            default: return 209715200L;
        }
    }

    @Transactional
    public StorageQuotaDto getOrCreate(UUID userId) {
        var sub = subscriptionRepo.findTopByUserIdAndStatusOrderByExpiryDateDesc(userId, "ACTIVE").orElse(null);
        String planCode = sub != null ? sub.getPlanCode() : "FREE";
        String planName = sub != null ? sub.getPlanName() : "Free";
        long limit = sub != null && sub.getStorageLimitBytes() != null ? sub.getStorageLimitBytes() : limitForPlan(planCode);

        var storage = storageRepo.findByUserId(userId).orElse(null);
        if (storage == null) {
            long used = documentRepo.sumUsedBytes(userId);
            storage = UserStorage.builder()
                    .userId(userId)
                    .allocatedBytes(limit)
                    .usedBytes(used)
                    .build();
            storage = storageRepo.save(storage);
        } else {
            // sync limit if plan changed
            if (!storage.getAllocatedBytes().equals(limit)) {
                storage.setAllocatedBytes(limit);
                storage = storageRepo.save(storage);
            }
        }
        // recalculate used
        long used = documentRepo.sumUsedBytes(userId);
        if (!storage.getUsedBytes().equals(used)) {
            storage.setUsedBytes(used);
            storage.setLastCalculatedAt(java.time.LocalDateTime.now());
            storage = storageRepo.save(storage);
        }
        long remaining = Math.max(0, limit - used);
        int percent = limit == 0 ? 0 : (int) Math.min(100, used * 100 / limit);
        return StorageQuotaDto.builder()
                .allocatedBytes(limit)
                .usedBytes(used)
                .remainingBytes(remaining)
                .usagePercentage(percent)
                .planCode(planCode)
                .planName(planName)
                .build();
    }

    @Transactional
    public void recalculate(UUID userId) {
        var storage = storageRepo.findByUserId(userId).orElse(null);
        if (storage == null) {
            getOrCreate(userId);
            return;
        }
        long used = documentRepo.sumUsedBytes(userId);
        storage.setUsedBytes(used);
        storage.setLastCalculatedAt(java.time.LocalDateTime.now());
        storageRepo.save(storage);
    }

    public boolean canUpload(UUID userId, long fileSize) {
        var quota = getOrCreate(userId);
        return quota.getUsedBytes() + fileSize <= quota.getAllocatedBytes();
    }

    @Transactional
    public void incrementUsed(UUID userId, long delta) {
        var storage = storageRepo.findByUserId(userId).orElseGet(() -> {
            var s = getOrCreate(userId);
            return storageRepo.findByUserId(userId).orElse(null);
        });
        if (storage != null) {
            storage.setUsedBytes(Math.max(0, storage.getUsedBytes() + delta));
            storage.setLastCalculatedAt(java.time.LocalDateTime.now());
            storageRepo.save(storage);
        }
    }
}
