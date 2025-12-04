package com.pm.taskapp.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT authentication entry point for handling authentication errors.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        log.error("Unauthorized error: {}", authException.getMessage());
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", Instant.now().toString());
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", authException.getMessage());
        errorDetails.put("path", request.getServletPath());
        
        // Add specific error details based on exception type
        if (authException.getMessage().contains("expired")) {
            errorDetails.put("errorCode", "TOKEN_EXPIRED");
            errorDetails.put("message", "Your session has expired. Please login again.");
        } else if (authException.getMessage().contains("JWT")) {
            errorDetails.put("errorCode", "INVALID_TOKEN");
            errorDetails.put("message", "Invalid authentication token");
        } else {
            errorDetails.put("errorCode", "AUTHENTICATION_REQUIRED");
            errorDetails.put("message", "Full authentication is required to access this resource");
        }
        
        objectMapper.writeValue(response.getOutputStream(), errorDetails);
    }
}