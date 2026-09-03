package com.officebuddy.jobswitch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSwitchDownloadDetailsDto {
    private String id;
    private String userId;
    private String packId;
    private Integer downloadCount;
    private Boolean active;
    private String deletedAt;
    private String createdAt;
    private String updatedAt;
    private String downloadedAt;
    private String ipAddress;
    private String selectedTypesSnapshot;
}
