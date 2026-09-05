package com.officebuddy.timeline;

import com.officebuddy.company.Company;
import com.officebuddy.company.CompanyRepository;
import com.officebuddy.timeline.dto.TimelineEventRequest;
import com.officebuddy.timeline.dto.TimelineEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TimelineRepository timelineRepository;
    private final CompanyRepository companyRepository;

    public List<TimelineEventResponse> getTimeline(UUID userId) {
        return timelineRepository.findByUserIdOrderByEventDateDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TimelineEventResponse> getTimelineByCompany(UUID userId, UUID companyId) {
        return timelineRepository.findByCompanyIdAndUserIdOrderByEventDateDesc(companyId, userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TimelineEventResponse addEvent(UUID userId, TimelineEventRequest request) {
        var event = TimelineEvent.builder()
                .userId(userId)
                .companyId(request.getCompanyId())
                .title(request.getTitle())
                .description(request.getDescription())
                .eventType(request.getEventType())
                .companyName(request.getCompanyName())
                .eventDate(request.getEventDate())
                .documentDate(request.getDocumentDate())
                .uploadedAt(request.getUploadedAt())
                .build();

        timelineRepository.save(event);
        return toResponse(event);
    }

    public int getExperienceYears(UUID userId) {
        var companies = companyRepository.findByUserIdOrderByStartDateDesc(userId);
        if (companies.isEmpty()) return 0;

        var earliest = companies.get(companies.size() - 1);
        var startYear = earliest.getStartDate().getYear();
        var now = YearMonth.now();
        return Math.max(1, now.getYear() - startYear);
    }

    private TimelineEventResponse toResponse(TimelineEvent event) {
        return TimelineEventResponse.builder()
                .id(event.getId().toString())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .companyName(event.getCompanyName())
                .eventDate(event.getEventDate() != null ? event.getEventDate().toString() : null)
                .documentDate(event.getDocumentDate() != null ? event.getDocumentDate().toString() : null)
                .uploadedAt(event.getUploadedAt() != null ? event.getUploadedAt().toString() : event.getCreatedAt() != null ? event.getCreatedAt().toString() : null)
                .build();
    }
}
