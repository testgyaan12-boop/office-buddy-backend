package com.officebuddy.document;

import com.officebuddy.document.dto.DocumentRequest;
import com.officebuddy.document.dto.DocumentResponse;
import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            Authentication authentication,
            @RequestParam(required = false) UUID companyId
    ) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(documentService.getDocuments(user.getId(), companyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(documentService.getDocument(user.getId(), id));
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam("companyId") UUID companyId,
            @RequestParam(value = "documentDate", required = false) String documentDate,
            @RequestParam(value = "tags", required = false) List<String> tags
    ) {
        var user = (User) authentication.getPrincipal();
        var request = DocumentRequest.builder()
                .fileName(file.getOriginalFilename())
                .type(DocumentType.valueOf(type))
                .companyId(companyId)
                .tags(tags)
                .build();
        return ResponseEntity.ok(documentService.uploadDocument(user.getId(), file, request));
    }

    @GetMapping("/presigned-url/{id}")
    public ResponseEntity<String> getPresignedUrl(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(documentService.getPresignedUrl(user.getId(), id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentResponse>> search(
            Authentication authentication,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) DocumentType type
    ) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(documentService.search(user.getId(), q, type));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        var user = (User) authentication.getPrincipal();
        documentService.deleteDocument(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
