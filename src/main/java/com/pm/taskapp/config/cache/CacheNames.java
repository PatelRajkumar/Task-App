package com.pm.taskapp.config.cache;

/**
 * Constants for cache names used throughout the application.
 * Centralized definition ensures consistency and prevents typos.
 * 
 * <p>
 * Cache Key Patterns:
 * <ul>
 * <li>USER_DETAILS: "user:details:{userId}"</li>
 * <li>PROJECT_MEMBER: "project:member:{projectId}:{userId}"</li>
 * </ul>
 * 
 * <p>
 * Each cache has a defined TTL (Time To Live) configured in RedisConfig:
 * <ul>
 * <li>USER_DETAILS: 15 minutes (900 seconds)</li>
 * <li>PROJECT_MEMBER: 5 minutes (300 seconds)</li>
 * </ul>
 * 
 * @author TaskApp Team
 * @since 1.0.0
 */
public final class CacheNames {

    /**
     * Private constructor to prevent instantiation.
     */
    private CacheNames() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Cache for user details (UserPrincipal with roles and permissions).
     * 
     * <p>
     * Used by: JWT authentication filter (CustomUserDetailsService.loadUserById)
     * <p>
     * TTL: 15 minutes (1/4 of JWT expiration time)
     * <p>
     * Key Pattern: "user:details:{userId}"
     * <p>
     * Evicted on: role change, account disable/lock, user deletion, logout
     * 
     * <p>
     * Example key: "taskapp::user:details:550e8400-e29b-41d4-a716-446655440000"
     */
    public static final String USER_DETAILS = "user:details";

    /**
     * Cache for project membership checks (boolean - is user a member?).
     * 
     * <p>
     * Used by: Authorization checks on every protected project endpoint
     * <p>
     * TTL: 5 minutes (faster propagation for membership changes)
     * <p>
     * Key Pattern: "project:member:{projectId}:{userId}"
     * <p>
     * Evicted on: member add/remove, role change, project deletion
     * 
     * <p>
     * Example key: "taskapp::project:member:proj-id:user-id"
     * 
     * <p>
     * Note: Only positive results (true) are cached. If user is NOT a member,
     * the cache is not populated - this is a fail-safe strategy where cache absence
     * forces a DB check.
     */
    public static final String PROJECT_MEMBER = "project:member";
}