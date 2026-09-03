package com.officebuddy.document;

import com.officebuddy.company.Company;
import com.officebuddy.company.CompanyRepository;
import com.officebuddy.document.dto.DocumentRequest;
import com.officebuddy.document.dto.DocumentResponse;
import com.officebuddy.storage.StorageService;
import com.officebuddy.timeline.TimelineService;
import com.officebuddy.timeline.dto.TimelineEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CompanyRepository companyRepository;
    private final StorageService storageService;
    private final TimelineService timelineService;

    public List<DocumentResponse> getDocuments(UUID userId, UUID companyId) {
        List<Document> documents;
        if (companyId != null) {
            documents = documentRepository.findByCompanyIdAndUserIdOrderByUploadedAtDesc(companyId, userId);
        } else {
            documents = documentRepository.findByUserIdOrderByUploadedAtDesc(userId);
        }
        return documents.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public DocumentResponse getDocument(UUID userId, UUID documentId) {
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        if (document.getDeletedAt() != null) {
            throw new RuntimeException("Document not found");
        }
        if (!document.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        return toResponse(document);
    }

    public DocumentResponse uploadDocument(
            UUID userId,
            MultipartFile file,
            DocumentRequest request
    ) {
        var storageResult = storageService.uploadFile(file);

        var document = Document.builder()
                .userId(userId)
                .companyId(request.getCompanyId())
                .title(request.getFileName())
                .fileName(file.getOriginalFilename())
                .type(request.getType())
                .fileKey(storageResult.getKey())
                .fileUrl(storageResult.getUrl())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .documentDate(request.getDocumentDate())
                .tags(request.getTags())
                .build();

        documentRepository.save(document);

        var company = document.getCompanyId() != null
                ? companyRepository.findById(document.getCompanyId()).orElse(null)
                : null;
        var companyName = company != null ? company.getName() : "Unknown";
        var eventRequest = new TimelineEventRequest();
        eventRequest.setCompanyId(document.getCompanyId());
        eventRequest.setCompanyName(companyName);
        eventRequest.setEventDate(document.getDocumentDate() != null ? document.getDocumentDate() : document.getUploadedAt().toLocalDate());

        switch (document.getType()) {
            case OFFER_LETTER:
                eventRequest.setTitle("Received offer from " + companyName);
                eventRequest.setEventType("OFFER_RECEIVED");
                break;
            case JOINING_LETTER:
                eventRequest.setTitle("Joined " + companyName);
                eventRequest.setEventType("COMPANY_JOINED");
                break;
            case INCREMENT_LETTER:
                eventRequest.setTitle("Increment at " + companyName);
                eventRequest.setEventType("INCREMENT");
                break;
            case PAYSLIP:
                eventRequest.setTitle("Salary record at " + companyName);
                eventRequest.setEventType("PAYSLIP");
                break;
            case CERTIFICATE:
                eventRequest.setTitle("Certificate from " + companyName);
                eventRequest.setEventType("CERTIFICATE");
                break;
            case RELIEVING_LETTER:
                eventRequest.setTitle("Relieved from " + companyName);
                eventRequest.setEventType("RELIEVED");
                break;
            default:
                eventRequest.setTitle("Document uploaded for " + companyName);
                eventRequest.setEventType("DOCUMENT_UPLOADED");
                break;
        }
        timelineService.addEvent(userId, eventRequest);

        return toResponse(document);
    }

    public String getPresignedUrl(UUID userId, UUID documentId) {
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        if (document.getDeletedAt() != null) {
            throw new RuntimeException("Document not found");
        }
        if (!document.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        return storageService.getPresignedUrl(document.getFileKey());
    }

    public void deleteDocument(UUID userId, UUID documentId) {
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        if (!document.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        if (document.getDeletedAt() != null) {
            throw new RuntimeException("Document already deleted");
        }
        // Soft delete only — keep file in storage (delete=false)
        document.setDeletedAt(java.time.LocalDateTime.now());
        documentRepository.save(document);
    }

    public List<DocumentResponse> search(UUID userId, String query, DocumentType type) {
        return documentRepository.search(userId, query, type)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public long getDocumentCount(UUID userId) {
        return documentRepository.countByUserId(userId);
    }

    public long getCertificateCount(UUID userId) {
        return documentRepository.countByUserIdAndType(userId);
    }

	private DocumentResponse toResponse(Document document) {

		String companyName = (document.getCompanyId() != null)
				? companyRepository.findById(document.getCompanyId()).map(c -> c.getName()).orElse(null)
				: null;

		return DocumentResponse.builder().id(document.getId().toString()).title(document.getTitle())
				.fileName(document.getFileName()).type(document.getType())
				.companyId(document.getCompanyId() != null ? document.getCompanyId().toString() : null)
				.companyName(companyName).fileUrl(document.getFileUrl()).fileKey(document.getFileKey())
				.fileSize(document.getFileSize()).tags(document.getTags())
				.uploadedAt(document.getUploadedAt().toString()).build();
	}
}
