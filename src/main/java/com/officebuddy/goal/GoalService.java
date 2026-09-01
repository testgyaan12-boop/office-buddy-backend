package com.officebuddy.goal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;

    public List<GoalDto> getGoals(UUID userId) {
        return goalRepository.findByUserIdAndDeletedAtIsNull(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<GoalDto> getDashboardGoals(UUID userId) {
        return goalRepository.findActiveByUserId(userId)
                .stream()
                .map(this::toDto)
                .limit(5)
                .collect(Collectors.toList());
    }

    public GoalDto createGoal(UUID userId, GoalDto request) {
        var goal = Goal.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .targetDate(LocalDate.parse(request.getTargetDate()))
                .category(request.getCategory() != null ? request.getCategory() : "custom")
                .status("active")
                .build();

        goalRepository.save(goal);
        return toDto(goal);
    }

    public GoalDto updateGoal(UUID userId, UUID goalId, GoalDto request) {
        var goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        if (!goal.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (request.getTitle() != null) goal.setTitle(request.getTitle());
        if (request.getDescription() != null) goal.setDescription(request.getDescription());
        if (request.getTargetDate() != null) goal.setTargetDate(LocalDate.parse(request.getTargetDate()));
        if (request.getCategory() != null) goal.setCategory(request.getCategory());
        if (request.getStatus() != null) goal.setStatus(request.getStatus());

        goalRepository.save(goal);
        return toDto(goal);
    }

    public void deleteGoal(UUID userId, UUID goalId) {
        var goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        if (!goal.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        goal.setDeletedAt(LocalDateTime.now());
        goalRepository.save(goal);
    }

    private GoalDto toDto(Goal goal) {
        long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), goal.getTargetDate());
        return GoalDto.builder()
                .id(goal.getId().toString())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .targetDate(goal.getTargetDate().toString())
                .category(goal.getCategory())
                .status(goal.getStatus())
                .remainingDays(remainingDays)
                .createdAt(goal.getCreatedAt().toString())
                .build();
    }
}
