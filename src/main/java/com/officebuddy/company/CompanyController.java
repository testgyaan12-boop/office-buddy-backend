package com.officebuddy.company;

import com.officebuddy.company.dto.CompanyRequest;
import com.officebuddy.company.dto.CompanyResponse;
import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getCompanies(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(companyService.getCompanies(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompany(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(companyService.getCompany(user.getId(), id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompanyResponse> createCompany(
            Authentication authentication,
            @RequestBody CompanyRequest request
    ) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(companyService.createCompany(user.getId(), request));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompanyResponse> updateCompany(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody CompanyRequest request
    ) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(companyService.updateCompany(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        var user = (User) authentication.getPrincipal();
        companyService.deleteCompany(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
