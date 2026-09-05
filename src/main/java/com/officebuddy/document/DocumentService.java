package com.officebuddy.document;

import com.officebuddy.company.Company;
import com.officebuddy.company.CompanyRepository;
import com.officebuddy.document.dto.DocumentRequest;
import com.officebuddy.document.dto.DocumentResponse;
import com.officebuddy.lookup.Lookup;
import com.officebuddy.lookup.LookupRepository;
import com.officebuddy.storage.StorageService;
import com.officebuddy.timeline.TimelineRepository;
import com.officebuddy.timeline.TimelineService;
import com.officebuddy.timeline.dto.TimelineEventRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CompanyRepository companyRepository;
    private final StorageService storageService;
    private final TimelineRepository timelineRepository;
    private final TimelineService timelineService;
    private final LookupRepository lookupRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        // Dynamic lookup: fetch eventType/title from lookups table remarks JSON, fallback to hard-coded
        String eventType = "DOCUMENT_UPLOADED";
        String titlePrefix = "Document uploaded for ";
        try {
            var lookupOpt = lookupRepository.findByLookupCode(document.getType());
            if (lookupOpt.isPresent() && lookupOpt.get().getRemarks() != null) {
                Map<String, Object> map = objectMapper.readValue(lookupOpt.get().getRemarks(), Map.class);
                if (map.containsKey("eventType")) eventType = (String) map.get("eventType");
                if (map.containsKey("title")) titlePrefix = (String) map.get("title");
            } else {
                // Fallback hard-coded for backward compat if lookup missing remarks
                String t = document.getType();
                if ("OFFER_LETTER".equals(t)) { eventType = "OFFER_RECEIVED"; titlePrefix = "Received offer from "; }
                else if ("JOINING_LETTER".equals(t)) { eventType = "COMPANY_JOINED"; titlePrefix = "Joined "; }
                else if ("INCREMENT_LETTER".equals(t)) { eventType = "INCREMENT"; titlePrefix = "Increment at "; }
                else if ("PAYSLIP".equals(t)) { eventType = "PAYSLIP"; titlePrefix = "Salary record at "; }
                else if ("CERTIFICATE".equals(t)) { eventType = "CERTIFICATE"; titlePrefix = "Certificate from "; }
                else if ("RELIEVING_LETTER".equals(t)) { eventType = "RELIEVED"; titlePrefix = "Relieved from "; }
                else if ("TDS_CERTIFICATE".equals(t)) { eventType = "CERTIFICATE"; titlePrefix = "TDS Certificate from "; }
                else if ("CONFIRMATION_LETTER".equals(t)) { eventType = "CONFIRMED"; titlePrefix = "Confirmation at "; }
            }
        } catch (Exception ex) {
            // Fallback hard-coded
            String t = document.getType();
            if ("OFFER_LETTER".equals(t)) { eventType = "OFFER_RECEIVED"; titlePrefix = "Received offer from "; }
            else if ("JOINING_LETTER".equals(t)) { eventType = "COMPANY_JOINED"; titlePrefix = "Joined "; }
            else if ("INCREMENT_LETTER".equals(t)) { eventType = "INCREMENT"; titlePrefix = "Increment at "; }
            else if ("PAYSLIP".equals(t)) { eventType = "PAYSLIP"; titlePrefix = "Salary record at "; }
            else if ("CERTIFICATE".equals(t)) { eventType = "CERTIFICATE"; titlePrefix = "Certificate from "; }
            else if ("RELIEVING_LETTER".equals(t)) { eventType = "RELIEVED"; titlePrefix = "Relieved from "; }
            else if ("TDS_CERTIFICATE".equals(t)) { eventType = "CERTIFICATE"; titlePrefix = "TDS Certificate from "; }
            else if ("CONFIRMATION_LETTER".equals(t)) { eventType = "CONFIRMED"; titlePrefix = "Confirmation at "; }
        }
        eventRequest.setTitle(titlePrefix + companyName);
        eventRequest.setEventType(eventType);
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
        var now = LocalDateTime.now();
        document.setDeletedAt(now);
        documentRepository.save(document);

        // Also soft delete timeline event for this document (so recent & timeline hide)
        try {
            if (document.getCompanyId() != null) {
                String expectedType = mapDocTypeToEventType(document.getType());
                var events = timelineRepository.findByCompanyIdAndUserIdOrderByEventDateDesc(
                        document.getCompanyId(), userId);
            for (var e : events) {
                if (e.getDeletedAt() != null) continue;
                if (expectedType.equals(e.getEventType())) {
                    // Match by company and type, delete most recent one
                    e.setDeletedAt(now);
                    e.setUpdatedAt(now);
                    timelineRepository.save(e);
                    break;
                }
            }
            // Fallback: if no exact type match, delete most recent event for this company
            if (events.stream().noneMatch(ev -> expectedType.equals(ev.getEventType()) && ev.getDeletedAt() != null)) {
                // already handled, no-op
            }
            }
        } catch (Exception ex) {
            // Timeline soft delete failure should not block document delete
        }
    }

    private String mapDocTypeToEventType(String docType) {
        if (docType == null) return "DOCUMENT_UPLOADED";
        return switch (docType) {
            case "OFFER_LETTER" -> "OFFER_RECEIVED";
            case "JOINING_LETTER" -> "COMPANY_JOINED";
            case "INCREMENT_LETTER" -> "INCREMENT";
            case "PAYSLIP" -> "PAYSLIP";
            case "CERTIFICATE" -> "CERTIFICATE";
            case "RELIEVING_LETTER" -> "RELIEVED";
            case "TDS_CERTIFICATE" -> "CERTIFICATE";
            case "CONFIRMATION_LETTER" -> "CONFIRMED";
            default -> "DOCUMENT_UPLOADED";
        };
    }

    public List<DocumentResponse> search(UUID userId, String query, String type) {
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
				.fileSize(document.getFileSize()).mimeType(document.getMimeType()).tags(document.getTags())
				.documentDate(document.getDocumentDate() != null ? document.getDocumentDate().toString() : null)
				.uploadedAt(document.getUploadedAt() != null ? document.getUploadedAt().toString() : null).build();
	}
}
