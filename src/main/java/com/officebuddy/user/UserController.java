package com.officebuddy.user;

import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getProfile(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user.toDto());
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(
            Authentication authentication,
            @RequestBody UserDto request
    ) {
        var user = (User) authentication.getPrincipal();
        var updated = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> uploadAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        var user = (User) authentication.getPrincipal();
        var updated = userService.uploadAvatar(user.getId(), file);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request
    ) {
        var user = (User) authentication.getPrincipal();
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam("q") String query) {
        var users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }
}
