package com.pm.taskapp.config.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * AOP aspect for logging cache operations.
 * Logs cache hits, misses, and evictions for debugging and monitoring.
 * 
 * <p>
 * Only active when logging level is set to DEBUG for cache operations:
 * 
 * <pre>
 * logging.level.org.springframework.cache = DEBUG
 * </pre>
 * 
 * <p>
 * Example log output:
 * 
 * <pre>
 * [DEBUG] Cache operation [CustomUserDetailsService.loadUserById(..)] completed in 45ms
 * [DEBUG] Cache eviction triggered by: UserServiceImpl.updateUserRoles(..)
 * [DEBUG] Cache eviction completed for: UserServiceImpl.updateUserRoles(..)
 * </pre>
 * 
 * @author TaskApp Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CacheLoggingAspect {

    private final CacheManager cacheManager;

    /**
     * Log @Cacheable method invocations with execution time.
     * 
     * @param joinPoint The intercepted method
     * @return The method result
     * @throws Throwable If the intercepted method throws an exception
     */
    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object logCacheable(ProceedingJoinPoint joinPoint) throws Throwable {
        // Skip logging if DEBUG is not enabled
        if (!log.isDebugEnabled()) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.debug("Cache operation [{}] completed in {}ms", methodName, duration);
            return result;

        } catch (Throwable ex) {
            log.error("Cache operation [{}] failed: {}", methodName, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Log @CacheEvict operations.
     * 
     * @param joinPoint The intercepted method
     * @return The method result
     * @throws Throwable If the intercepted method throws an exception
     */
    @Around("@annotation(org.springframework.cache.annotation.CacheEvict)")
    public Object logCacheEvict(ProceedingJoinPoint joinPoint) throws Throwable {
        // Skip logging if DEBUG is not enabled
        if (!log.isDebugEnabled()) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().toShortString();

        log.debug("Cache eviction triggered by: {}", methodName);

        Object result = joinPoint.proceed();

        log.debug("Cache eviction completed for: {}", methodName);

        return result;
    }

    /**
     * Log @CachePut operations (if used in the future).
     * 
     * @param joinPoint The intercepted method
     * @return The method result
     * @throws Throwable If the intercepted method throws an exception
     */
    @Around("@annotation(org.springframework.cache.annotation.CachePut)")
    public Object logCachePut(ProceedingJoinPoint joinPoint) throws Throwable {
        // Skip logging if DEBUG is not enabled
        if (!log.isDebugEnabled()) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().toShortString();

        log.debug("Cache update triggered by: {}", methodName);

        Object result = joinPoint.proceed();

        log.debug("Cache update completed for: {}", methodName);

        return result;
    }
}