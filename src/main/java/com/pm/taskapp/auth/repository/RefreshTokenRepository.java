package com.pm.taskapp.auth.repository;

import com.pm.taskapp.auth.enitity.RefreshToken;
import com.pm.taskapp.auth.enitity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Lookup refresh token entity by token string
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Remove a single user's tokens (useful on logout / revoke).
     * Returns number of removed rows (if using derived delete query).
     */
    long deleteByUser(User user);

    /**
     * Delete by user id (alternative).
     */
    long deleteAllByUser_Id(UUID userId);

    // Count tokens for a user
    long countByUser_Id(UUID userId);

    // Find tokens for a user ordered by expiration
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId ORDER BY rt.expiresAt ASC")
    List<RefreshToken> findByUserIdOrderByExpiresAtAsc(@Param("userId") UUID userId);

    // Delete expired tokens
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") Instant now);

    // Alternative: Find all expired tokens
    List<RefreshToken> findByExpiresAtBefore(Instant now);
}