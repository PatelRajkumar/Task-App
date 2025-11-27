package com.pm.taskapp.auth.service.impl;

import com.pm.taskapp.auth.dto.*;
import com.pm.taskapp.auth.enitity.RefreshToken;
import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.auth.exception.*;
import com.pm.taskapp.auth.mapper.UserMapper;
import com.pm.taskapp.auth.security.UserPrincipal;
import com.pm.taskapp.auth.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of AuthenticationService for authentication operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenService jwtTokenService;
    private final UserMapper userMapper;
    private final EmailService emailService; // You'll need to implement this

    @Value("${app.auth.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.auth.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    // In-memory storage for failed attempts (consider using Redis in production)
    private final Map<String, FailedLoginAttempt> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, String> passwordResetTokens = new ConcurrentHashMap<>();
    private final Map<String, String> emailVerificationTokens = new ConcurrentHashMap<>();

    @Override
    public UserResponseDTO register(RegisterRequestDTO registerRequest) {
        log.info("Registering new user with email: {}", registerRequest.getEmail());

        // Validate registration data
        validateRegistrationData(registerRequest);

        // Create user
        UserCreateDTO createDTO = UserCreateDTO.builder()
                .email(registerRequest.getEmail())
                .name(registerRequest.getName())
                .password(registerRequest.getPassword())
                .avatarUrl(registerRequest.getAvatarUrl())
                .build();

        UserResponseDTO newUser = userService.createUser(createDTO);

        // Send verification email
        sendVerificationEmail(newUser.getEmail());

        log.info("User registered successfully: {}", newUser.getId());
        return newUser;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        // Check if account is locked
        if (isAccountLocked(loginRequest.getEmail())) {
            throw new AccountLockedException("Account is locked due to too many failed login attempts");
        }

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // Reset failed attempts on successful login
            resetFailedLoginAttempts(loginRequest.getEmail());

            // Update last login
            userService.updateLastLogin(userPrincipal.getId());

            // Generate tokens
            String accessToken = jwtTokenService.generateAccessToken(userPrincipal);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());

            // Get user details
            User user = userService.findUserById(userPrincipal.getId());

            log.info("User logged in successfully: {}", userPrincipal.getId());

            return LoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenService.getAccessTokenExpirationMs())
                    .user(userMapper.toResponseDTO(user))
                    .build();

        } catch (BadCredentialsException e) {
            recordFailedLoginAttempt(loginRequest.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        } catch (DisabledException e) {
            throw new AccountDisabledException("Account is disabled");
        }
    }

    @Override
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequest) {
        log.debug("Refreshing token");

        // Validate and get refresh token
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenRequest.getRefreshToken());
        User user = refreshToken.getUser();

        // Generate new access token
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String newAccessToken = jwtTokenService.generateAccessToken(userPrincipal);

        // Optionally rotate refresh token
        if (refreshTokenRequest.isRotateRefreshToken()) {
            refreshTokenService.deleteRefreshToken(refreshToken);
            refreshToken = refreshTokenService.createRefreshToken(user.getId());
        }

        log.info("Token refreshed for user: {}", user.getId());

        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getAccessTokenExpirationMs())
                .user(userMapper.toResponseDTO(user))
                .build();
    }

    @Override
    public void logout(UUID userId) {
        log.info("Logging out user: {}", userId);

        // Invalidate refresh tokens
        refreshTokenService.deleteByUserId(userId);

        // Clear security context
        SecurityContextHolder.clearContext();

        log.info("User logged out successfully: {}", userId);
    }

    @Override
    public void logoutAllDevices(UUID userId) {
        log.info("Logging out user from all devices: {}", userId);

        // Delete all refresh tokens for user
        refreshTokenService.deleteAllUserTokens(userId);

        log.info("User logged out from all devices: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return jwtTokenService.validateToken(token);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPrincipal getUserPrincipalFromToken(String token) {
        UUID userId = jwtTokenService.getUserIdFromToken(token);
        User user = userService.getUserWithRolesAndPermissions(userId);
        return UserPrincipal.create(user);
    }

    @Override
    public void initiatePasswordReset(String email) {
        log.info("Initiating password reset for email: {}", email);

        try {
            User user = userService.findUserByEmail(email);

            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            passwordResetTokens.put(resetToken, user.getId().toString());

            // Send reset email
            emailService.sendPasswordResetEmail(email, resetToken);

            log.info("Password reset email sent to: {}", email);
        } catch (ResourceNotFoundException e) {
            // Don't reveal if email exists or not for security
            log.warn("Password reset requested for non-existent email: {}", email);
        }
    }

    @Override
    public void resetPasswordWithToken(String token, String newPassword) {
        log.info("Resetting password with token");

        String userId = passwordResetTokens.get(token);
        if (userId == null) {
            throw new InvalidTokenException("Invalid or expired reset token");
        }

        // Reset password
        userService.resetPassword(UUID.fromString(userId), newPassword);

        // Remove used token
        passwordResetTokens.remove(token);

        log.info("Password reset successfully for user: {}", userId);
    }

    @Override
    public void verifyEmail(String token) {
        log.info("Verifying email with token");

        String email = emailVerificationTokens.get(token);
        if (email == null) {
            throw new InvalidTokenException("Invalid or expired verification token");
        }

        User user = userService.findUserByEmail(email);
        userService.setUserEnabled(user.getId(), true);

        // Remove used token
        emailVerificationTokens.remove(token);

        log.info("Email verified successfully for user: {}", user.getId());
    }

    @Override
    public void resendVerificationEmail(String email) {
        log.info("Resending verification email to: {}", email);

        User user = userService.findUserByEmail(email);

        if (user.isEnabled()) {
            throw new IllegalStateException("Email is already verified");
        }

        sendVerificationEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAccountLocked(String email) {
        FailedLoginAttempt attempt = failedAttempts.get(email);
        if (attempt == null) {
            return false;
        }

        // Check if lockout period has expired
        if (attempt.isLocked()) {
            Instant lockoutExpiry = attempt.getLastAttemptTime()
                    .plus(lockoutDurationMinutes, ChronoUnit.MINUTES);

            if (Instant.now().isAfter(lockoutExpiry)) {
                // Lockout expired, reset attempts
                failedAttempts.remove(email);
                return false;
            }
            return true;
        }

        return false;
    }

    @Override
    public void recordFailedLoginAttempt(String email) {
        FailedLoginAttempt attempt = failedAttempts.computeIfAbsent(email,
                k -> new FailedLoginAttempt());

        attempt.incrementAttempts();
        attempt.setLastAttemptTime(Instant.now());

        if (attempt.getAttemptCount() >= maxFailedAttempts) {
            attempt.setLocked(true);
            log.warn("Account locked due to {} failed login attempts: {}",
                    maxFailedAttempts, email);
        }
    }

    @Override
    public void resetFailedLoginAttempts(String email) {
        failedAttempts.remove(email);
    }

    // Helper methods

    private void validateRegistrationData(RegisterRequestDTO request) {
        // Check if email already exists
        if (userService.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        // Validate password strength
        if (!isPasswordStrong(request.getPassword())) {
            throw new WeakPasswordException("Password does not meet security requirements");
        }

        // Validate password confirmation
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new ValidationException("Passwords do not match");
        }
    }

    private boolean isPasswordStrong(String password) {
        // Implement password strength validation
        // Minimum 8 characters, at least one uppercase, one lowercase, one number, one special character
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        return password.matches(passwordRegex);
    }

    private void sendVerificationEmail(String email) {
        String verificationToken = UUID.randomUUID().toString();
        emailVerificationTokens.put(verificationToken, email);
        emailService.sendEmailVerification(email, verificationToken);
    }

    // Inner class for tracking failed login attempts
    private static class FailedLoginAttempt {
        private int attemptCount = 0;
        private Instant lastAttemptTime;
        private boolean locked = false;

        public void incrementAttempts() {
            this.attemptCount++;
        }

        // Getters and setters
        public int getAttemptCount() { return attemptCount; }
        public Instant getLastAttemptTime() { return lastAttemptTime; }
        public void setLastAttemptTime(Instant time) { this.lastAttemptTime = time; }
        public boolean isLocked() { return locked; }
        public void setLocked(boolean locked) { this.locked = locked; }
    }
}