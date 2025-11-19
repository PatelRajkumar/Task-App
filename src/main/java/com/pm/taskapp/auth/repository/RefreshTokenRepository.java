package com.pm.taskapp.auth.repository;

import com.pm.taskapp.auth.enitity.RefreshToken;
import com.pm.taskapp.auth.enitity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}