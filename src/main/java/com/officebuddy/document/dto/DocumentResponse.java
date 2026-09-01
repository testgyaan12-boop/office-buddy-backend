package com.officebuddy.document.dto;

import com.officebuddy.document.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private String id;
    private String title;
    private String fileName;
    private DocumentType type;
    private String companyId;
    private String companyName;
    private String fileUrl;
    private String fileKey;
    private long fileSize;
    private List<String> tags;
    private String uploadedAt;
}
