package com.officebuddy.jobswitch;

import com.officebuddy.jobswitch.dto.JobSwitchDownloadDetailsDto;
import com.officebuddy.jobswitch.dto.JobSwitchGenerateRequest;
import com.officebuddy.jobswitch.dto.JobSwitchPackDto;
import com.officebuddy.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/job-switch")
@RequiredArgsConstructor
public class JobSwitchController {

    private final JobSwitchService jobSwitchService;

    @PostMapping("/generate")
    public ResponseEntity<JobSwitchPackDto> generatePack(
            Authentication authentication,
            @RequestBody(required = false) JobSwitchGenerateRequest request) {
        var user = (User) authentication.getPrincipal();
        if (request == null) {
            return ResponseEntity.ok(jobSwitchService.generatePack(user.getId()));
        }
        return ResponseEntity.ok(jobSwitchService.generatePack(user.getId(), request));
    }

    @GetMapping("/status/{packId}")
    public ResponseEntity<JobSwitchPackDto> getPackStatus(
            Authentication authentication,
            @PathVariable UUID packId) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(jobSwitchService.getPackStatusById(user.getId(), packId));
    }

    @GetMapping("/status")
    public ResponseEntity<JobSwitchPackDto> getLatestStatus(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(jobSwitchService.getPackStatus(user.getId()));
    }

    @PostMapping("/download/{packId}")
    public ResponseEntity<JobSwitchPackDto> recordDownload(
            Authentication authentication,
            @PathVariable UUID packId,
            HttpServletRequest request) {
        var user = (User) authentication.getPrincipal();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        return ResponseEntity.ok(jobSwitchService.recordDownload(user.getId(), packId, ip, ua));
    }

    @GetMapping("/pack-download-details")
    public ResponseEntity<List<JobSwitchDownloadDetailsDto>> getDownloadDetails(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(jobSwitchService.getDownloadDetails(user.getId()));
    }

    @GetMapping("/pack-download-details/all")
    public ResponseEntity<List<JobSwitchDownloadDetailsDto>> getAllDownloadDetails(Authentication authentication) {
        // For admin/feasibility — returns all non-deleted rows
        return ResponseEntity.ok(jobSwitchService.getAllDownloadDetails());
    }

    @DeleteMapping("/pack-download-details/{detailId}")
    public ResponseEntity<Void> softDeleteDownloadDetail(
            Authentication authentication,
            @PathVariable UUID detailId) {
        var user = (User) authentication.getPrincipal();
        jobSwitchService.softDeleteDownloadDetail(user.getId(), detailId);
        return ResponseEntity.noContent().build();
    }
}
