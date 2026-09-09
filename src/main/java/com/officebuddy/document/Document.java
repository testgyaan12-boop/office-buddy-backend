package com.officebuddy.document;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private UUID companyId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String type;

    private String fileKey;

    private String fileUrl;

    private long fileSize;

    private String mimeType;

    private LocalDate documentDate;

    @ElementCollection
    @CollectionTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime uploadedAt;

    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
