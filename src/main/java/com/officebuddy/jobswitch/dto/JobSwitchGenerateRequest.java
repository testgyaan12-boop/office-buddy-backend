package com.officebuddy.jobswitch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSwitchGenerateRequest {
    // Map of lookup_code -> count (0 = off, >0 = kitna) e.g. OFFER_LETTER -> 2
    private Map<String, Integer> includeCounts;
}
