package com.pm.taskapp.auth.service.impl;

import com.pm.taskapp.auth.exception.InvalidTokenException;
import com.pm.taskapp.auth.security.UserPrincipal;
import com.pm.taskapp.auth.service.JwtTokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of JwtTokenService for JWT operations.
 */
@Slf4j
@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    @Value("${app.auth.jwt-secret}")
    private String jwtSecret;

    @Value("${app.auth.jwt-expiration-ms:3600000}") // 1 hour default
    private long jwtExpirationMs;

    @Value("${app.auth.password-reset-token-expiration-ms:3600000}") // 1 hour
    private long passwordResetTokenExpirationMs;

    @Value("${app.auth.email-verification-token-expiration-ms:86400000}") // 24 hours
    private long emailVerificationTokenExpirationMs;

    private static final String TOKEN_TYPE = "JWT";
    private static final String TOKEN_ISSUER = "taskapp-auth-service";
    private static final String TOKEN_AUDIENCE = "taskapp-client";

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_RESET = "reset";
    private static final String TOKEN_TYPE_VERIFY = "verify";

    @Override
    public String generateAccessToken(UserPrincipal userPrincipal) {
        log.debug("Generating access token for user: {}", userPrincipal.getId());

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userPrincipal.getId().toString());
        claims.put(CLAIM_EMAIL, userPrincipal.getEmail());
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);

        // Add roles
        List<String> roles = userPrincipal.getAuthorities().stream()
                .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());
        claims.put(CLAIM_ROLES, roles);

        // Add permissions
        List<String> permissions = userPrincipal.getAuthorities().stream()
                .filter(auth -> !auth.getAuthority().startsWith("ROLE_"))
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());
        claims.put(CLAIM_PERMISSIONS, permissions);

        return createToken(claims, userPrincipal.getEmail(), jwtExpirationMs);
    }

    @Override
    public String generateTokenWithClaims(UserPrincipal userPrincipal, Map<String, Object> additionalClaims) {
        log.debug("Generating token with custom claims for user: {}", userPrincipal.getId());

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userPrincipal.getId().toString());
        claims.put(CLAIM_EMAIL, userPrincipal.getEmail());

        // Add additional claims
        if (additionalClaims != null) {
            claims.putAll(additionalClaims);
        }

        return createToken(claims, userPrincipal.getEmail(), jwtExpirationMs);
    }

    @Override
    public UUID getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        String userIdStr = claims.get(CLAIM_USER_ID, String.class);
        return UUID.fromString(userIdStr);
    }

    @Override
    public String getUsernameFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getExpiration();
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // Return claims even if token is expired (for debugging/logging)
            return e.getClaims();
        } catch (Exception e) {
            throw new InvalidTokenException("Failed to parse JWT token", e);
        }
    }

    @Override
    public Object getClaimFromToken(String token, String claimName) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get(claimName);
    }

    @Override
    public long getAccessTokenExpirationMs() {
        return jwtExpirationMs;
    }

    @Override
    public String generatePasswordResetToken(String email) {
        log.debug("Generating password reset token for email: {}", email);

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_EMAIL, email);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_RESET);

        return createToken(claims, email, passwordResetTokenExpirationMs);
    }

    @Override
    public String generateEmailVerificationToken(String email) {
        log.debug("Generating email verification token for email: {}", email);

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_EMAIL, email);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_VERIFY);

        return createToken(claims, email, emailVerificationTokenExpirationMs);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        if (!validateToken(token)) {
            throw new InvalidTokenException("Invalid password reset token");
        }

        Claims claims = getAllClaimsFromToken(token);
        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);

        if (!TOKEN_TYPE_RESET.equals(tokenType)) {
            throw new InvalidTokenException("Token is not a password reset token");
        }

        return claims.get(CLAIM_EMAIL, String.class);
    }

    @Override
    public String validateEmailVerificationToken(String token) {
        if (!validateToken(token)) {
            throw new InvalidTokenException("Invalid email verification token");
        }

        Claims claims = getAllClaimsFromToken(token);
        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);

        if (!TOKEN_TYPE_VERIFY.equals(tokenType)) {
            throw new InvalidTokenException("Token is not an email verification token");
        }

        return claims.get(CLAIM_EMAIL, String.class);
    }

    // Helper methods

    private String createToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(TOKEN_ISSUER)
                .setAudience(TOKEN_AUDIENCE)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(UUID.randomUUID().toString())
                .setHeaderParam("typ", TOKEN_TYPE)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get(CLAIM_ROLES, List.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get(CLAIM_PERMISSIONS, List.class);
    }
}