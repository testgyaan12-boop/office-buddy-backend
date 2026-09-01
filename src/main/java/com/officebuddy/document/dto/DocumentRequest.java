package com.officebuddy.document.dto;

import com.officebuddy.document.DocumentType;
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
    private DocumentType type;
    private UUID companyId;
    private LocalDate documentDate;
    private List<String> tags;
}
