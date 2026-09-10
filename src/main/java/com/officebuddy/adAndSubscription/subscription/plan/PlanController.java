package com.officebuddy.adAndSubscription.subscription.plan;

import com.officebuddy.adAndSubscription.subscription.plan.entity.Plan;
import com.officebuddy.adAndSubscription.subscription.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanRepository planRepo;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        var plans = planRepo.findAll().stream()
                .filter(p -> Integer.valueOf(1).equals(p.getIsActive()) && !Integer.valueOf(1).equals(p.getIsDeleted()))
                .map(p -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("planName", p.getPlanName());
                    m.put("planCode", p.getPlanCode());
                    m.put("period", p.getPeriod());
                    m.put("allocatedBytes", p.getAllocatedBytes());
                    m.put("allocatedUnit", p.getAllocatedUnit());
                    m.put("amountPaise", p.getAmountPaise());
                    m.put("currency", p.getCurrency());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(plans);
    }
}
