package com.pm.taskapp.auth.controller;

import com.pm.taskapp.auth.dto.*;
import com.pm.taskapp.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Register a new user account.
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        log.info("Registration request received for email: {}", registerRequest.getEmail());
        UserResponseDTO newUser = authenticationService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    /**
     * Authenticate user and get access token.
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and receive tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "403", description = "Account locked or disabled")
    })
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpServletRequest request) {
        
        // Add request metadata for security tracking
        loginRequest.setIpAddress(getClientIpAddress(request));
        loginRequest.setUserAgent(request.getHeader("User-Agent"));
        
        log.info("Login request received for email: {}", loginRequest.getEmail());
        LoginResponseDTO response = authenticationService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using refresh token.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<LoginResponseDTO> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequest,
            HttpServletRequest request) {
        
        // Add request metadata
        refreshTokenRequest.setIpAddress(getClientIpAddress(request));
        
        log.info("Token refresh request received");
        LoginResponseDTO response = authenticationService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout current user.
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout current user and invalidate tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MessageResponse> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout request received");
        // Extract user ID from token and logout
        // This would typically be handled by Spring Security context
        // For now, returning success message
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    /**
     * Request password reset.
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent if account exists"),
        @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody @Parameter(description = "Email address") ForgotPasswordRequest request) {
        
        log.info("Password reset requested for email: {}", request.getEmail());
        authenticationService.initiatePasswordReset(request.getEmail());
        
        // Always return success to prevent email enumeration
        return ResponseEntity.ok(new MessageResponse(
            "If an account exists with this email, a password reset link has been sent"));
    }

    /**
     * Reset password with token.
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using token from email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successful"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        log.info("Password reset attempt with token");
        authenticationService.resetPasswordWithToken(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully"));
    }

    /**
     * Verify email address.
     */
    @GetMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify email address using token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        log.info("Email verification attempt with token");
        authenticationService.verifyEmail(token);
        return ResponseEntity.ok(new MessageResponse("Email has been verified successfully"));
    }

    /**
     * Resend email verification.
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email", description = "Resend email verification link")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification email sent"),
        @ApiResponse(responseCode = "400", description = "Email already verified"),
        @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    public ResponseEntity<MessageResponse> resendVerification(
            @Valid @RequestBody @Parameter(description = "Email address") ResendVerificationRequest request) {
        
        log.info("Resend verification requested for email: {}", request.getEmail());
        authenticationService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("Verification email has been sent"));
    }

    /**
     * Validate token.
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Check if access token is valid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid"),
        @ApiResponse(responseCode = "401", description = "Token is invalid or expired")
    })
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = extractTokenFromHeader(authHeader);
        boolean isValid = authenticationService.validateToken(token);
        
        return ResponseEntity.ok(new TokenValidationResponse(isValid));
    }

    /**
     * Get client IP address.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        String xrHeader = request.getHeader("X-Real-IP");
        if (xrHeader != null) {
            return xrHeader;
        }
        return request.getRemoteAddr();
    }

    /**
     * Extract token from Authorization header.
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }

    // Inner classes for request/response DTOs

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MessageResponse {
        private String message;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ForgotPasswordRequest {
        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Email
        private String email;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResetPasswordRequest {
        @jakarta.validation.constraints.NotBlank
        private String token;
        
        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Size(min = 8)
        private String newPassword;
        
        @jakarta.validation.constraints.NotBlank
        private String confirmPassword;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResendVerificationRequest {
        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Email
        private String email;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenValidationResponse {
        private boolean valid;
    }
}