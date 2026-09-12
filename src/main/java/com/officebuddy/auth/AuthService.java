package com.officebuddy.auth;

import com.officebuddy.adAndSubscription.subscription.service.SubscriptionService;
import com.officebuddy.auth.dto.*;
import com.officebuddy.security.SecurityConfigService;
import com.officebuddy.auth.security.JwtService;
import com.officebuddy.user.User;
import com.officebuddy.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final SubscriptionService subscriptionService;
    private final SecurityConfigService securityConfigService;

    private static final int OTP_EXPIRY_MINUTES = 15;

    public Map<String, String> register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        if (!request.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            throw new RuntimeException("Password must be at least 8 characters with 1 uppercase, 1 lowercase, 1 number, and 1 special character");
        }

        var verificationToken = UUID.randomUUID().toString();

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .verificationToken(verificationToken)
                .accessRole("member")
                .build();

        var savedUser = userRepository.save(user);
        user = savedUser;

        try {
            subscriptionService.ensureFreeSubscription(user.getId());
        } catch (Exception e) {
            log.warn("Failed to create FREE subscription for {}: {}", user.getEmail(), e.getMessage());
        }

        log.info("Verification token for {}: {}", user.getEmail(), verificationToken);

        try {
            String emailResponse = emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationToken);
            log.info("Verification email response for {}: {}", user.getEmail(), emailResponse);
        } catch (Exception e) {
            log.warn("Verification email failed for {}: {}", user.getEmail(), e.getMessage());
        }

        return Map.of("message", "Registration successful. Please check your email to verify your account.");
    }

    public Map<String, String> verifyEmail(VerifyEmailRequest request) {
        var user = userRepository.findByVerificationToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        try {
            subscriptionService.ensureFreeSubscription(user.getId());
        } catch (Exception e) {
            log.warn("Failed to ensure FREE subscription for {}: {}", user.getEmail(), e.getMessage());
        }

        return Map.of("message", "Email verified successfully. You can now log in.");
    }

    public Map<String, String> resendVerification(ResendVerificationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        var newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        userRepository.save(user);

        log.info("Resent verification token for {}: {}", user.getEmail(), newToken);

        try {
           String emailRes = emailService.sendVerificationEmail(user.getEmail(), user.getName(), newToken);
            log.error("resend verification email to {}: {}", user.getEmail(), emailRes);
        } catch (Exception e) {
            log.error("Failed to resend verification email to {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to send verification email. Please try again.");
        }

        return Map.of("message", "Verification email resent. Please check your inbox.");
    }

    public AuthResponse login(LoginRequest request) {
        var preUser = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (preUser != null && preUser.getAccountLockedUntil() != null) {
            if (preUser.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Account locked due to multiple failed login attempts. Try again after " + fmtLockTime(preUser.getAccountLockedUntil()) + ".");
            }
            preUser.setAccountLockedUntil(null);
            preUser.setFailedLoginAttempts(0);
            userRepository.save(preUser);
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            LocalDateTime lockedUntil = recordFailedAttempt(request.getEmail());
            if (lockedUntil != null) {
                throw new RuntimeException("Account locked due to multiple failed login attempts. Try again after " + fmtLockTime(lockedUntil) + ".");
            }
            int remaining = remainingAttempts(request.getEmail());
            if (remaining > 0) {
                throw new RuntimeException("Invalid email or password. " + remaining + (remaining == 1 ? " attempt" : " attempts") + " remaining before account lock.");
            }
            throw new RuntimeException("Invalid email or password");
        }

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new RuntimeException("Your account has been deleted. Please contact support.");
        }

        if (user.getIsActive() == null || user.getIsActive() != 1) {
            throw new RuntimeException("Your account is inactive. Please contact support.");
        }

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(user.toDto())
                .build();
    }

    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("If this email is registered, you will receive a password reset OTP"));

        var otp = String.format("%06d", new Random().nextInt(999999));
        user.setResetOtp(otp);
        user.setResetOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        userRepository.save(user);

        log.info("Password reset OTP for {}: {}", user.getEmail(), otp);

        try {
            emailService.sendResetOtp(user.getEmail(), user.getName(), otp);
        } catch (Exception e) {
            log.error("Failed to send reset OTP to {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to send reset email. Please try again.");
        }

        return Map.of("message", "If this email is registered, you will receive a password reset OTP");
    }

    public Map<String, String> resetPassword(ResetPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid request"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getResetOtpExpiry() == null || user.getResetOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setEmailVerified(true);
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        userRepository.save(user);

        return Map.of("message", "Password reset successfully. You can now log in with your new password.");
    }

    private LocalDateTime recordFailedAttempt(String email) {
        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return null;
        int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
        user.setFailedLoginAttempts(attempts);
        int maxAttempts = securityConfigService.getInt(SecurityConfigService.LOGIN_MAX_ATTEMPTS, 5);
        if (attempts < maxAttempts) {
            userRepository.save(user);
            return null;
        }
        int lockHours = securityConfigService.getInt(SecurityConfigService.LOGIN_LOCK_HOURS, 12);
        LocalDateTime lockedUntil = LocalDateTime.now().plusHours(lockHours);
        user.setAccountLockedUntil(lockedUntil);
        userRepository.save(user);
        if (securityConfigService.isEnabled(SecurityConfigService.LOGIN_LOCK_EMAIL, true)) {
            try {
                String resp = emailService.sendAccountLockedEmail(user.getEmail(), user.getName(), lockHours, fmtLockTime(lockedUntil));
                log.info("Account lock email response for {}: {}", user.getEmail(), resp);
            } catch (Exception e) {
                log.warn("Account lock email failed for {}: {}", user.getEmail(), e.getMessage());
            }
        }
        return lockedUntil;
    }

    private int remainingAttempts(String email) {
        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return -1;
        int maxAttempts = securityConfigService.getInt(SecurityConfigService.LOGIN_MAX_ATTEMPTS, 5);
        int used = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        return Math.max(0, maxAttempts - used);
    }

    private String fmtLockTime(LocalDateTime t) {
        return t.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    public AuthResponse refreshToken(String refreshToken) {
        var email = jwtService.extractUsername(refreshToken);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new RuntimeException("Invalid refresh token");
        }

        if (user.getAccountLockedUntil() != null) {
            if (user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Account locked due to multiple failed login attempts. Try again after " + fmtLockTime(user.getAccountLockedUntil()) + ".");
            }
            user.setAccountLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        if (Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new RuntimeException("Your account has been deleted. Please contact support.");
        }

        if (user.getIsActive() == null || user.getIsActive() != 1) {
            throw new RuntimeException("Your account is inactive. Please contact support.");
        }

        var newAccessToken = jwtService.generateToken(user);
        var newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(user.toDto())
                .build();
    }
}
