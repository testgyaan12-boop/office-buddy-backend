package com.officebuddy.adAndSubscription.customad;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/custom-ads")
@RequiredArgsConstructor
public class CustomAdController {

    private final CustomAdRepository repo;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> active() {
        var list = repo.findByIsActiveAndIsDeletedOrderByIdDesc(1, 0).stream().map(ad -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", ad.getId());
            m.put("title", ad.getTitle());
            m.put("productImgLink", ad.getProductImgLink());
            m.put("productOpenLink", ad.getProductOpenLink());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
