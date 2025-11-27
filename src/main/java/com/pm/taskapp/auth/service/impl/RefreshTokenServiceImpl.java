package com.pm.taskapp.auth.service.impl;

import com.pm.taskapp.auth.enitity.RefreshToken;
import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.auth.exception.InvalidTokenException;
import com.pm.taskapp.auth.exception.ResourceNotFoundException;
import com.pm.taskapp.auth.repository.RefreshTokenRepository;
import com.pm.taskapp.auth.repository.UserRepository;
import com.pm.taskapp.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Implementation of RefreshTokenService for managing refresh tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${app.auth.refresh-token-expiration-days:30}")
    private long refreshTokenExpirationDays;

    @Value("${app.auth.max-refresh-tokens-per-user:5}")
    private int maxRefreshTokensPerUser;

    @Override
    public RefreshToken createRefreshToken(UUID userId) {
        log.debug("Creating refresh token for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user has too many refresh tokens
        long tokenCount = countUserTokens(userId);
        if (tokenCount >= maxRefreshTokensPerUser) {
            log.info("User {} has {} tokens, removing oldest", userId, tokenCount);
            // Note: You might want to implement removeOldestToken method
            deleteOldestUserToken(userId);
        }

        // Generate unique token
        String token = generateUniqueToken();

        // Calculate expiration
        Instant expiresAt = Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created for user: {} with expiry: {}", userId, expiresAt);

        return savedToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {
        log.debug("Verifying refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        // Check if token is expired
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Refresh token expired for user: {}", refreshToken.getUser().getId());
            // Delete expired token
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidTokenException("Refresh token has expired");
        }

        log.debug("Refresh token verified successfully");
        return refreshToken;
    }

    @Override
    public void deleteRefreshToken(RefreshToken refreshToken) {
        log.debug("Deleting refresh token: {}", refreshToken.getId());
        refreshTokenRepository.delete(refreshToken);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        log.info("Deleting refresh tokens for user: {}", userId);
        long deletedCount = refreshTokenRepository.deleteAllByUser_Id(userId);
        log.info("Deleted {} refresh tokens for user: {}", deletedCount, userId);
    }

    @Override
    @Scheduled(cron = "${app.auth.token-cleanup-cron:0 0 2 * * ?}") // Run daily at 2 AM
    public void deleteExpiredTokens() {
        log.info("Starting expired token cleanup job");

        // Note: You'll need to add this method to RefreshTokenRepository
        // @Modifying
        // @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
        // void deleteExpiredTokens(@Param("now") Instant now);

        // For now, we'll fetch and delete individually
        refreshTokenRepository.findAll().stream()
                .filter(token -> token.getExpiresAt().isBefore(Instant.now()))
                .forEach(token -> {
                    log.debug("Deleting expired token: {}", token.getId());
                    refreshTokenRepository.delete(token);
                });

        log.info("Completed expired token cleanup");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTokenValid(String token) {
        try {
            RefreshToken refreshToken = verifyRefreshToken(token);
            return refreshToken != null;
        } catch (InvalidTokenException e) {
            return false;
        }
    }

    @Override
    public RefreshToken extendTokenExpiration(String token) {
        log.debug("Extending token expiration");

        RefreshToken refreshToken = verifyRefreshToken(token);

        // Extend expiration
        Instant newExpiry = Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS);
        refreshToken.setExpiresAt(newExpiry);

        RefreshToken updatedToken = refreshTokenRepository.save(refreshToken);
        log.info("Extended token expiration to: {}", newExpiry);

        return updatedToken;
    }

    @Override
    @Transactional(readOnly = true)
    public long countUserTokens(UUID userId) {
        // Note: You'll need to add this method to RefreshTokenRepository
        // long countByUser_Id(UUID userId);

        return refreshTokenRepository.findAll().stream()
                .filter(token -> token.getUser().getId().equals(userId))
                .count();
    }

    @Override
    public void deleteAllUserTokens(UUID userId) {
        log.info("Deleting all tokens for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        long deletedCount = refreshTokenRepository.deleteByUser(user);
        log.info("Deleted {} tokens for user: {}", deletedCount, userId);
    }

    // Helper methods

    private String generateUniqueToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "") +
                    UUID.randomUUID().toString().replace("-", "");
        } while (refreshTokenRepository.findByToken(token).isPresent());

        return token;
    }

    private void deleteOldestUserToken(UUID userId) {
        // Note: You'll need to add this method to RefreshTokenRepository to find oldest token
        // @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId ORDER BY rt.expiresAt ASC")
        // List<RefreshToken> findByUserIdOrderByExpiresAtAsc(@Param("userId") UUID userId);

        refreshTokenRepository.findAll().stream()
                .filter(token -> token.getUser().getId().equals(userId))
                .min((t1, t2) -> t1.getExpiresAt().compareTo(t2.getExpiresAt()))
                .ifPresent(oldestToken -> {
                    log.debug("Deleting oldest token: {}", oldestToken.getId());
                    refreshTokenRepository.delete(oldestToken);
                });
    }
}