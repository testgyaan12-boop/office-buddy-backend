package com.officebuddy.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String avatarUrl;
    private String headline;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String currentCompany;
    private String salary;
    private String expectedSalary;
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String bloodGroup;
    private String linkedInUrl;
    private String portfolioUrl;
    private String panNumber;
    private String aadhaarNumber;
    private String uanNumber;
    private String pfNumber;
    private String bankAccountNumber;
    private String ifscCode;
    private String emergencyContact;

    @Builder.Default
    private boolean emailVerified = false;

    private String verificationToken;

    private String resetOtp;

    private LocalDateTime resetOtpExpiry;

    private String resetToken;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public UserDto toDto() {
        return UserDto.builder()
                .id(id.toString())
                .name(name)
                .email(email)
                .avatarUrl(avatarUrl)
                .headline(headline)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .phone(phone)
                .currentCompany(currentCompany)
                .salary(salary)
                .expectedSalary(expectedSalary)
                .skills(skills)
                .address(address)
                .bloodGroup(bloodGroup)
                .linkedInUrl(linkedInUrl)
                .portfolioUrl(portfolioUrl)
                .panNumber(panNumber)
                .aadhaarNumber(aadhaarNumber)
                .uanNumber(uanNumber)
                .pfNumber(pfNumber)
                .bankAccountNumber(bankAccountNumber)
                .ifscCode(ifscCode)
                .emergencyContact(emergencyContact)
                .build();
    }
}
