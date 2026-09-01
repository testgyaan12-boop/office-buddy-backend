package com.officebuddy.todo;

import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    public ResponseEntity<List<TodoDto>> getTodos(
            Authentication authentication,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String goalId
    ) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(todoService.getTodos(user.getId(), type, search, date, goalId));
    }

    @PostMapping
    public ResponseEntity<TodoDto> createTodo(
            Authentication authentication,
            @RequestBody TodoDto request
    ) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(todoService.createTodo(user.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoDto> updateTodo(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody TodoDto request
    ) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(todoService.updateTodo(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        var user = (User) authentication.getPrincipal();
        todoService.deleteTodo(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
