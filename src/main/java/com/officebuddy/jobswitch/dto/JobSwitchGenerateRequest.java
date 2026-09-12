package com.officebuddy.jobswitch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSwitchGenerateRequest {
    // Map of lookup_code -> count (0 = off, >0 = kitna) e.g. OFFER_LETTER -> 2
    private Map<String, Integer> includeCounts;
    // Optional filters: company ids (empty = all), date range yyyy-MM-dd (null = no bound)
    private List<String> companyIds;
    private String fromDate;
    private String toDate;
}
