package com.officebuddy.auth;

import com.officebuddy.adAndSubscription.subscription.service.SubscriptionService;
import com.officebuddy.auth.dto.*;
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
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
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

    public AuthResponse refreshToken(String refreshToken) {
        var email = jwtService.extractUsername(refreshToken);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new RuntimeException("Invalid refresh token");
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
