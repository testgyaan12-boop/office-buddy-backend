package com.officebuddy.user;

import com.officebuddy.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    public UserDto updateProfile(UUID userId, UserDto request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getHeadline() != null) user.setHeadline(request.getHeadline());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getCurrentCompany() != null) user.setCurrentCompany(request.getCurrentCompany());
        if (request.getSalary() != null) user.setSalary(request.getSalary());
        if (request.getExpectedSalary() != null) user.setExpectedSalary(request.getExpectedSalary());
        if (request.getSkills() != null) user.setSkills(request.getSkills());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getBloodGroup() != null) user.setBloodGroup(request.getBloodGroup());
        if (request.getLinkedInUrl() != null) user.setLinkedInUrl(request.getLinkedInUrl());
        if (request.getPortfolioUrl() != null) user.setPortfolioUrl(request.getPortfolioUrl());
        if (request.getPanNumber() != null) user.setPanNumber(request.getPanNumber());
        if (request.getAadhaarNumber() != null) user.setAadhaarNumber(request.getAadhaarNumber());
        if (request.getUanNumber() != null) user.setUanNumber(request.getUanNumber());
        if (request.getPfNumber() != null) user.setPfNumber(request.getPfNumber());
        if (request.getBankAccountNumber() != null) user.setBankAccountNumber(request.getBankAccountNumber());
        if (request.getIfscCode() != null) user.setIfscCode(request.getIfscCode());
        if (request.getEmergencyContact() != null) user.setEmergencyContact(request.getEmergencyContact());

        userRepository.save(user);
        return user.toDto();
    }

    public UserDto uploadAvatar(UUID userId, MultipartFile file) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            var originalName = file.getOriginalFilename();
            var extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            var fileName = "avatars/" + userId + "_" + System.currentTimeMillis() + extension;
            var result = storageService.uploadBytes(fileName, file.getBytes(), file.getContentType());
            user.setAvatarUrl(result.getUrl());
            userRepository.save(user);
            return user.toDto();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload avatar", e);
        }
    }

    public void changePassword(UUID userId, ChangePasswordRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            throw new RuntimeException("Current password is required");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new RuntimeException("New password is required");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }
        String newPass = request.getNewPassword();
        if (!newPass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            throw new RuntimeException("Password must be at least 8 characters with 1 uppercase, 1 lowercase, 1 number, and 1 special character");
        }
        if (passwordEncoder.matches(newPass, user.getPasswordHash())) {
            throw new RuntimeException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPass));
        userRepository.save(user);
    }

    public List<UserDto> searchUsers(String query) {
        var users = userRepository.findByNameContainingIgnoreCase(query);
        return users.stream()
                .map(user -> UserDto.builder()
                        .id(user.getId().toString())
                        .name(user.getName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .headline(user.getHeadline())
                        .skills(user.getSkills())
                        .build())
                .toList();
    }
}
