package com.officebuddy.company;

import com.officebuddy.company.dto.CompanyRequest;
import com.officebuddy.company.dto.CompanyResponse;
import com.officebuddy.document.DocumentRepository;
import com.officebuddy.timeline.TimelineRepository;
import com.officebuddy.timeline.TimelineService;
import com.officebuddy.timeline.dto.TimelineEventRequest;
import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final DocumentRepository documentRepository;
    private final TimelineRepository timelineRepository;
    private final TimelineService timelineService;

    public List<CompanyResponse> getCompanies(UUID userId) {
        return companyRepository.findByUserIdOrderByStartDateDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CompanyResponse getCompany(UUID userId, UUID companyId) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        if (company.getDeletedAt() != null) {
            throw new RuntimeException("Company not found");
        }
        if (!company.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        return toResponse(company);
    }

    public CompanyResponse createCompany(UUID userId, CompanyRequest request) {
        boolean current = Boolean.TRUE.equals(request.getIsCurrent());
        var company = Company.builder()
                .userId(userId)
                .name(request.getName())
                .role(request.getRole())
                .startDate(request.getStartDate())
                .endDate(current ? null : request.getEndDate())
                .isCurrent(current)
                .build();

        companyRepository.save(company);

        var eventRequest = new TimelineEventRequest();
        eventRequest.setCompanyId(company.getId());
        eventRequest.setTitle("Joined " + request.getName());
        eventRequest.setDescription("Started working as " + request.getRole());
        eventRequest.setEventType("COMPANY_JOINED");
        eventRequest.setCompanyName(request.getName());
        eventRequest.setEventDate(request.getStartDate());
        timelineService.addEvent(userId, eventRequest);

        return toResponse(company);
    }

    public CompanyResponse updateCompany(UUID userId, UUID companyId, CompanyRequest request) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        if (!company.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        boolean current = Boolean.TRUE.equals(request.getIsCurrent());
        company.setName(request.getName());
        company.setRole(request.getRole());
        company.setStartDate(request.getStartDate());
        company.setEndDate(current ? null : request.getEndDate());
        company.setCurrent(current);

        companyRepository.save(company);
        return toResponse(company);
    }

    public void deleteCompany(UUID userId, UUID companyId) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        if (!company.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        if (company.getDeletedAt() != null) {
            throw new RuntimeException("Company already deleted");
        }
        var now = LocalDateTime.now();
        company.setDeletedAt(now);
        companyRepository.save(company);

        // Soft delete career timeline events for this company
        var events = timelineRepository.findByCompanyIdAndUserIdOrderByEventDateDesc(companyId, userId);
        for (var e : events) {
            if (e.getDeletedAt() == null) {
                e.setDeletedAt(now);
            }
        }
        if (!events.isEmpty()) timelineRepository.saveAll(events);

        // Soft delete documents for this company (keep files, hide from list)
        var docs = documentRepository.findByCompanyIdAndUserIdOrderByUploadedAtDesc(companyId, userId);
        for (var d : docs) {
            if (d.getDeletedAt() == null) {
                d.setDeletedAt(now);
            }
        }
        if (!docs.isEmpty()) documentRepository.saveAll(docs);
    }

    public long getCompanyCount(UUID userId) {
        return companyRepository.countByUserId(userId);
    }

    private CompanyResponse toResponse(Company company) {
        long docCount = documentRepository.countByCompanyId(company.getId());
        return CompanyResponse.builder()
                .id(company.getId().toString())
                .name(company.getName())
                .role(company.getRole())
                .startDate(company.getStartDate())
                .endDate(company.getEndDate())
                .isCurrent(company.isCurrent())
                .documentCount((int) docCount)
                .createdAt(company.getCreatedAt().toString())
                .build();
    }
}
