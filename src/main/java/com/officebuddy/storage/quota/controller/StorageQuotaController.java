package com.officebuddy.storage.quota.controller;

import com.officebuddy.storage.quota.dto.StorageQuotaDto;
import com.officebuddy.storage.quota.service.StorageQuotaService;
import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/storage/quota")
@RequiredArgsConstructor
public class StorageQuotaController {

    private final StorageQuotaService quotaService;

    @GetMapping("/me")
    public ResponseEntity<StorageQuotaDto> me(Authentication auth) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(quotaService.getOrCreate(user.getId()));
    }

    @PostMapping("/recalculate")
    public ResponseEntity<StorageQuotaDto> recalculate(Authentication auth) {
        var user = (User) auth.getPrincipal();
        quotaService.recalculate(user.getId());
        return ResponseEntity.ok(quotaService.getOrCreate(user.getId()));
    }
}
