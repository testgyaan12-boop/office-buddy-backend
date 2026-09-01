package com.officebuddy.timeline;

import com.officebuddy.timeline.dto.TimelineEventRequest;
import com.officebuddy.timeline.dto.TimelineEventResponse;
import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/timeline")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping
    public ResponseEntity<List<TimelineEventResponse>> getTimeline(
            Authentication authentication,
            @RequestParam(required = false) UUID companyId
    ) {
        var user = (User) authentication.getPrincipal();
        if (companyId != null) {
            return ResponseEntity.ok(timelineService.getTimelineByCompany(user.getId(), companyId));
        }
        return ResponseEntity.ok(timelineService.getTimeline(user.getId()));
    }

    @PostMapping("/events")
    public ResponseEntity<TimelineEventResponse> addEvent(
            Authentication authentication,
            @RequestBody TimelineEventRequest request
    ) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(timelineService.addEvent(user.getId(), request));
    }
}
