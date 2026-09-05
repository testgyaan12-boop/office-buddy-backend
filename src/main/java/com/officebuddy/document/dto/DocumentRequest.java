package com.officebuddy.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {
    private String fileName;
    private String type;
    private UUID companyId;
    private LocalDate documentDate;
    private List<String> tags;
}
