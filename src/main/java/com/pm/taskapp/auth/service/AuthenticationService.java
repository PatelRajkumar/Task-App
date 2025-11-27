package com.pm.taskapp.auth.service;

import com.pm.taskapp.auth.dto.LoginRequestDTO;
import com.pm.taskapp.auth.dto.LoginResponseDTO;
import com.pm.taskapp.auth.dto.RefreshTokenRequestDTO;
import com.pm.taskapp.auth.dto.RegisterRequestDTO;
import com.pm.taskapp.auth.dto.UserResponseDTO;
import com.pm.taskapp.auth.security.UserPrincipal;

import java.util.UUID;

/**
 * Service interface for authentication operations.
 */
public interface AuthenticationService {

    /**
     * Register a new user.
     *
     * @param registerRequest Registration data
     * @return Created user response
     */
    UserResponseDTO register(RegisterRequestDTO registerRequest);

    /**
     * Authenticate user and generate tokens.
     *
     * @param loginRequest Login credentials
     * @return Login response with tokens
     */
    LoginResponseDTO login(LoginRequestDTO loginRequest);

    /**
     * Refresh access token using refresh token.
     *
     * @param refreshTokenRequest Refresh token request
     * @return New login response with tokens
     */
    LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequest);

    /**
     * Logout user and invalidate tokens.
     *
     * @param userId User ID
     */
    void logout(UUID userId);

    /**
     * Logout user from all devices by invalidating all tokens.
     *
     * @param userId User ID
     */
    void logoutAllDevices(UUID userId);

    /**
     * Validate access token.
     *
     * @param token JWT token
     * @return true if valid
     */
    boolean validateToken(String token);

    /**
     * Get user principal from token.
     *
     * @param token JWT token
     * @return User principal
     */
    UserPrincipal getUserPrincipalFromToken(String token);

    /**
     * Initiate password reset process.
     *
     * @param email User email
     */
    void initiatePasswordReset(String email);

    /**
     * Complete password reset with token.
     *
     * @param token Reset token
     * @param newPassword New password
     */
    void resetPasswordWithToken(String token, String newPassword);

    /**
     * Verify user email with verification token.
     *
     * @param token Verification token
     */
    void verifyEmail(String token);

    /**
     * Resend email verification.
     *
     * @param email User email
     */
    void resendVerificationEmail(String email);

    /**
     * Check if user account is locked.
     *
     * @param email User email
     * @return true if locked
     */
    boolean isAccountLocked(String email);

    /**
     * Record failed login attempt.
     *
     * @param email User email
     */
    void recordFailedLoginAttempt(String email);

    /**
     * Reset failed login attempts.
     *
     * @param email User email
     */
    void resetFailedLoginAttempts(String email);
}