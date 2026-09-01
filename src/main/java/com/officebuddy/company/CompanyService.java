package com.officebuddy.company;

import com.officebuddy.company.dto.CompanyRequest;
import com.officebuddy.company.dto.CompanyResponse;
import com.officebuddy.document.DocumentRepository;
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
        if (!company.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        return toResponse(company);
    }

    public CompanyResponse createCompany(UUID userId, CompanyRequest request) {
        var company = Company.builder()
                .userId(userId)
                .name(request.getName())
                .role(request.getRole())
                .startDate(request.getStartDate())
                .endDate(request.isCurrent() ? null : request.getEndDate())
                .isCurrent(request.isCurrent())
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

        company.setName(request.getName());
        company.setRole(request.getRole());
        company.setStartDate(request.getStartDate());
        company.setEndDate(request.isCurrent() ? null : request.getEndDate());
        company.setCurrent(request.isCurrent());

        companyRepository.save(company);
        return toResponse(company);
    }

    public void deleteCompany(UUID userId, UUID companyId) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        if (!company.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        company.setDeletedAt(LocalDateTime.now());
        companyRepository.save(company);
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
