package com.officebuddy.admin;

import com.officebuddy.user.User;
import com.officebuddy.user.UserDto;
import com.officebuddy.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<Page<UserDto>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @PageableDefault(size = 20) Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        java.util.Date f = parseDayStart(from);
        java.util.Date t = parseDayEnd(to);
        Page<User> page;
        if (query == null && f == null && t == null) {
            page = userRepository.findAll(pageable);
        } else {
            page = userRepository.searchAdmin(query, f, t, pageable);
        }
        return ResponseEntity.ok(page.map(User::toDto));
    }

    private java.util.Date parseDayStart(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return java.util.Date.from(java.time.LocalDate.parse(s.trim()).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) {
            return null;
        }
    }

    private java.util.Date parseDayEnd(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return java.util.Date.from(java.time.LocalDate.parse(s.trim()).plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().minusMillis(1));
        } catch (Exception ignored) {
            return null;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> get(@PathVariable UUID id) {
        var user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user.toDto());
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody Map<String, Object> body) {
        String name = body.get("name") != null ? body.get("name").toString().trim() : "";
        String email = body.get("email") != null ? body.get("email").toString().trim() : "";
        String password = body.get("password") != null ? body.get("password").toString() : "";
        String role = body.get("accessRole") != null ? body.get("accessRole").toString().toLowerCase() : "admin";
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) throw new RuntimeException("Name, email and password are required");
        if (!role.equals("admin") && !role.equals("member")) throw new RuntimeException("Invalid accessRole");
        if (userRepository.findByEmail(email).isPresent()) throw new RuntimeException("Email already registered");
        var user = User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .emailVerified(true)
                .accessRole(role)
                .build();
        return ResponseEntity.ok(userRepository.save(user).toDto());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        var user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (body.get("accessRole") != null) {
            String role = body.get("accessRole").toString().toLowerCase();
            if (!role.equals("admin") && !role.equals("member")) throw new RuntimeException("Invalid accessRole");
            user.setAccessRole(role);
        }
        if (body.get("isActive") != null) user.setIsActive(Integer.parseInt(body.get("isActive").toString()));
        if (body.get("isDeleted") != null) user.setIsDeleted(Integer.parseInt(body.get("isDeleted").toString()));
        if (body.get("failedLoginAttempts") != null) user.setFailedLoginAttempts(Integer.parseInt(body.get("failedLoginAttempts").toString()));
        if (body.containsKey("accountLockedUntil")) {
            Object v = body.get("accountLockedUntil");
            user.setAccountLockedUntil(v == null || v.toString().isBlank() ? null : java.time.LocalDateTime.parse(v.toString()));
        }
        return ResponseEntity.ok(userRepository.save(user).toDto());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        if (!userRepository.existsById(id)) throw new RuntimeException("User not found");
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
