package com.officebuddy.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lookups")
@RequiredArgsConstructor
public class LookupController {

    private final LookupService lookupService;

    @GetMapping
    public ResponseEntity<List<Lookup>> getLookups(@RequestParam(required = false) String code,
                                                   @RequestParam(required = false) String parentCode,
                                                   @RequestParam(required = false) Long parentId) {
        String effectiveCode = code != null ? code : parentCode;
        if (effectiveCode != null) {
            return ResponseEntity.ok(lookupService.getByParentCode(effectiveCode));
        }
        if (parentId != null) {
            return ResponseEntity.ok(lookupService.getChildren(parentId));
        }
        return ResponseEntity.ok(lookupService.getChildren(null));
    }

    @GetMapping("/{code}")
    public ResponseEntity<Lookup> getByCode(@PathVariable String code) {
        var lookup = lookupService.getByCode(code);
        if (lookup == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(lookup);
    }
}
