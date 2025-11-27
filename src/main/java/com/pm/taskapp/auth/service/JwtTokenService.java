package com.pm.taskapp.auth.service;

import com.pm.taskapp.auth.security.UserPrincipal;
import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for JWT token operations.
 */
public interface JwtTokenService {

    /**
     * Generate access token for user.
     *
     * @param userPrincipal User principal
     * @return JWT access token
     */
    String generateAccessToken(UserPrincipal userPrincipal);

    /**
     * Generate token with custom claims.
     *
     * @param userPrincipal User principal
     * @param additionalClaims Additional claims to include
     * @return JWT token
     */
    String generateTokenWithClaims(UserPrincipal userPrincipal, Map<String, Object> additionalClaims);

    /**
     * Extract user ID from token.
     *
     * @param token JWT token
     * @return User ID
     */
    UUID getUserIdFromToken(String token);

    /**
     * Extract username/email from token.
     *
     * @param token JWT token
     * @return Username/email
     */
    String getUsernameFromToken(String token);

    /**
     * Validate token.
     *
     * @param token JWT token
     * @return true if valid
     */
    boolean validateToken(String token);

    /**
     * Get token expiration date.
     *
     * @param token JWT token
     * @return Expiration date
     */
    Date getExpirationDateFromToken(String token);

    /**
     * Check if token is expired.
     *
     * @param token JWT token
     * @return true if expired
     */
    boolean isTokenExpired(String token);

    /**
     * Extract all claims from token.
     *
     * @param token JWT token
     * @return Token claims
     */
    Claims getAllClaimsFromToken(String token);

    /**
     * Get specific claim from token.
     *
     * @param token JWT token
     * @param claimName Claim name
     * @return Claim value
     */
    Object getClaimFromToken(String token, String claimName);

    /**
     * Get access token expiration time in milliseconds.
     *
     * @return Expiration time in ms
     */
    long getAccessTokenExpirationMs();

    /**
     * Generate token for password reset.
     *
     * @param email User email
     * @return Reset token
     */
    String generatePasswordResetToken(String email);

    /**
     * Generate token for email verification.
     *
     * @param email User email
     * @return Verification token
     */
    String generateEmailVerificationToken(String email);

    /**
     * Validate password reset token.
     *
     * @param token Reset token
     * @return Email if valid
     */
    String validatePasswordResetToken(String token);

    /**
     * Validate email verification token.
     *
     * @param token Verification token
     * @return Email if valid
     */
    String validateEmailVerificationToken(String token);
}