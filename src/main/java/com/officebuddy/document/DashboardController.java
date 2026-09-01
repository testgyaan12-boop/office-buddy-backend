package com.officebuddy.document;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final com.officebuddy.document.DocumentService documentService;
    private final com.officebuddy.company.CompanyService companyService;
    private final com.officebuddy.timeline.TimelineService timelineService;
    private final com.officebuddy.user.UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            org.springframework.security.core.Authentication authentication
    ) {
        var user = (com.officebuddy.user.User) authentication.getPrincipal();
        var userId = user.getId();

        long docCount = documentService.getDocumentCount(userId);
        long companyCount = companyService.getCompanyCount(userId);
        int experienceYears = timelineService.getExperienceYears(userId);
        long certificateCount = documentService.getCertificateCount(userId);

        return ResponseEntity.ok(Map.of(
                "totalDocuments", docCount,
                "totalCompanies", companyCount,
                "experienceYears", experienceYears,
                "totalCertificates", certificateCount
        ));
    }
}
