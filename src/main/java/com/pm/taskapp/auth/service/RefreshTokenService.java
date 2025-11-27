package com.pm.taskapp.auth.service;

import com.pm.taskapp.auth.enitity.RefreshToken;

import java.util.UUID;

/**
 * Service interface for refresh token operations.
 */
public interface RefreshTokenService {

    /**
     * Create a new refresh token for user.
     *
     * @param userId User ID
     * @return Created refresh token
     */
    RefreshToken createRefreshToken(UUID userId);

    /**
     * Verify refresh token and return if valid.
     *
     * @param token Refresh token string
     * @return Valid refresh token entity
     */
    RefreshToken verifyRefreshToken(String token);

    /**
     * Delete a specific refresh token.
     *
     * @param refreshToken Refresh token entity
     */
    void deleteRefreshToken(RefreshToken refreshToken);

    /**
     * Delete all refresh tokens for a user.
     *
     * @param userId User ID
     */
    void deleteByUserId(UUID userId);

    /**
     * Delete all expired refresh tokens (cleanup job).
     */
    void deleteExpiredTokens();

    /**
     * Check if refresh token exists and is valid.
     *
     * @param token Refresh token string
     * @return true if valid
     */
    boolean isTokenValid(String token);

    /**
     * Extend refresh token expiration.
     *
     * @param token Refresh token string
     * @return Updated refresh token
     */
    RefreshToken extendTokenExpiration(String token);

    /**
     * Count active refresh tokens for user.
     *
     * @param userId User ID
     * @return Number of active tokens
     */
    long countUserTokens(UUID userId);

    /**
     * Delete all tokens for user (logout from all devices).
     *
     * @param userId User ID
     */
    void deleteAllUserTokens(UUID userId);
}