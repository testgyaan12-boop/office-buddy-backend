package com.officebuddy.storage.quota.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageQuotaDto {
    private Long allocatedBytes;
    private Long usedBytes;
    private Long remainingBytes;
    private Integer usagePercentage;
    private String planCode;
    private String planName;
}
