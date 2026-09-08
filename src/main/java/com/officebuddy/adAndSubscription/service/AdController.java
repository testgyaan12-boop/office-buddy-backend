package com.officebuddy.adAndSubscription.service;

import com.officebuddy.adAndSubscription.config.dto.AdConfigRes;
import com.officebuddy.adAndSubscription.log.dto.AdRequestLogReq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ads")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;

    @GetMapping("/config")
    public ResponseEntity<?> config(
            Authentication auth,
            @RequestParam String platform,
            @RequestParam String placement,
            @RequestParam String adType) {
        var user = (org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal();
        // Extract userId via User principal (cast)
        var appUser = (com.officebuddy.user.User) auth.getPrincipal();
        AdConfigRes cfg = adService.getAdConfig(platform, placement, adType, appUser.getId());
        if (cfg == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(cfg);
    }

    @PostMapping("/log")
    public ResponseEntity<Void> log(Authentication auth, @RequestBody AdRequestLogReq req) {
        var user = (com.officebuddy.user.User) auth.getPrincipal();
        adService.recordLog(user.getId(), req.getProvider(), req.getPlacement(), req.getAdType(), req.getStatus(), req.getErrorCode());
        return ResponseEntity.ok().build();
    }
}
