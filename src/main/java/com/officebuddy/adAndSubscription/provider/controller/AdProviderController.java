package com.officebuddy.adAndSubscription.provider.controller;

import com.officebuddy.adAndSubscription.provider.dto.AdProviderDto;
import com.officebuddy.adAndSubscription.provider.dto.AdProviderReq;
import com.officebuddy.adAndSubscription.provider.service.AdProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ad-providers")
@RequiredArgsConstructor
public class AdProviderController {

    private final AdProviderService service;

    @PostMapping
    public ResponseEntity<AdProviderDto> create(Authentication auth, @RequestBody AdProviderReq req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdProviderDto> update(@PathVariable Long id, @RequestBody AdProviderReq req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @GetMapping
    public ResponseEntity<List<AdProviderDto>> list(@RequestParam(required = false) String platform) {
        return ResponseEntity.ok(service.list(platform));
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<Void> toggle(@PathVariable Long id) {
        service.toggleActive(id);
        return ResponseEntity.ok().build();
    }
}
