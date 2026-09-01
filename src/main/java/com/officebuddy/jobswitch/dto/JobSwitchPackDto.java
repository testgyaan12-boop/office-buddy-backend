package com.officebuddy.jobswitch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSwitchPackDto {
    private String id;
    private String status;
    private String downloadUrl;
}
