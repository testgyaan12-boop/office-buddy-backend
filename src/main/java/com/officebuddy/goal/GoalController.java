package com.officebuddy.goal;

import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    public ResponseEntity<List<GoalDto>> getGoals(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(goalService.getGoals(user.getId()));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<GoalDto>> getDashboardGoals(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(goalService.getDashboardGoals(user.getId()));
    }

    @PostMapping
    public ResponseEntity<GoalDto> createGoal(
            Authentication authentication,
            @RequestBody GoalDto request
    ) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(goalService.createGoal(user.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalDto> updateGoal(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody GoalDto request
    ) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(goalService.updateGoal(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        var user = (User) authentication.getPrincipal();
        goalService.deleteGoal(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
