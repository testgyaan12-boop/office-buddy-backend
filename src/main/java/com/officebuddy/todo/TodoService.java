package com.officebuddy.todo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    public List<TodoDto> getTodos(UUID userId, String type, String search, String date, String goalId) {
        if (goalId != null) {
            return todoRepository.findByUserIdAndGoalId(userId, UUID.fromString(goalId))
                    .stream().map(this::toDto).collect(Collectors.toList());
        }
        if (date != null) {
            return todoRepository.findByUserIdAndDueDate(userId, LocalDate.parse(date))
                    .stream().map(this::toDto).collect(Collectors.toList());
        }
        if (search != null && !search.isEmpty()) {
            return todoRepository.findByUserIdAndTitleContainingIgnoreCase(userId, search)
                    .stream().map(this::toDto).collect(Collectors.toList());
        }
        if (type != null && !type.isEmpty()) {
            return todoRepository.findByUserIdAndType(userId, type)
                    .stream().map(this::toDto).collect(Collectors.toList());
        }
        return todoRepository.findByUserId(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public TodoDto createTodo(UUID userId, TodoDto request) {
        var todo = Todo.builder()
                .userId(userId)
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType() != null ? request.getType() : "task")
                .build();

        if (request.getGoalId() != null) {
            todo.setGoalId(UUID.fromString(request.getGoalId()));
        }
        if (request.getDueDate() != null) {
            todo.setDueDate(LocalDate.parse(request.getDueDate()));
        }

        todoRepository.save(todo);
        return toDto(todo);
    }

    public TodoDto updateTodo(UUID userId, UUID todoId, TodoDto request) {
        var todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        if (!todo.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (request.getTitle() != null) todo.setTitle(request.getTitle());
        if (request.getContent() != null) todo.setContent(request.getContent());
        if (request.getType() != null) todo.setType(request.getType());
        if (request.getDueDate() != null) todo.setDueDate(LocalDate.parse(request.getDueDate()));

        todo.setCompleted(request.isCompleted());

        todoRepository.save(todo);
        return toDto(todo);
    }

    public void deleteTodo(UUID userId, UUID todoId) {
        var todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        if (!todo.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        todo.setDeletedAt(LocalDateTime.now());
        todoRepository.save(todo);
    }

    private TodoDto toDto(Todo todo) {
        return TodoDto.builder()
                .id(todo.getId().toString())
                .userId(todo.getUserId().toString())
                .goalId(todo.getGoalId() != null ? todo.getGoalId().toString() : null)
                .title(todo.getTitle())
                .content(todo.getContent())
                .type(todo.getType())
                .dueDate(todo.getDueDate() != null ? todo.getDueDate().toString() : null)
                .completed(todo.isCompleted())
                .createdAt(todo.getCreatedAt().toString())
                .build();
    }
}
