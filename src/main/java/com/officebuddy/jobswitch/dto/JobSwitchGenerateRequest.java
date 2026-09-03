package com.officebuddy.jobswitch.dto;

import com.officebuddy.document.DocumentType;
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
    // Map of DocumentType -> count (0 = off, >0 = kitna)
    private Map<DocumentType, Integer> includeCounts;
}
