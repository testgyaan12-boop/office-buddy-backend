package com.officebuddy.jobswitch;

import com.officebuddy.jobswitch.dto.JobSwitchPackDto;
import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/job-switch")
@RequiredArgsConstructor
public class JobSwitchController {

    private final JobSwitchService jobSwitchService;

    @PostMapping("/generate")
    public ResponseEntity<JobSwitchPackDto> generatePack(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(jobSwitchService.generatePack(user.getId()));
    }

    @GetMapping("/status/{packId}")
    public ResponseEntity<JobSwitchPackDto> getPackStatus(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(jobSwitchService.getPackStatus(user.getId()));
    }
}
