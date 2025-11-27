package com.pm.taskapp.auth.service;

import java.util.Map;

/**
 * Service interface for email operations.
 */
public interface EmailService {

    /**
     * Send email verification.
     *
     * @param email Recipient email
     * @param verificationToken Verification token
     */
    void sendEmailVerification(String email, String verificationToken);

    /**
     * Send password reset email.
     *
     * @param email Recipient email
     * @param resetToken Reset token
     */
    void sendPasswordResetEmail(String email, String resetToken);

    /**
     * Send welcome email to new user.
     *
     * @param email Recipient email
     * @param userName User name
     */
    void sendWelcomeEmail(String email, String userName);

    /**
     * Send account locked notification.
     *
     * @param email Recipient email
     * @param reason Lock reason
     */
    void sendAccountLockedEmail(String email, String reason);

    /**
     * Send password changed notification.
     *
     * @param email Recipient email
     */
    void sendPasswordChangedEmail(String email);

    /**
     * Send login alert for new device/location.
     *
     * @param email Recipient email
     * @param deviceInfo Device information
     * @param location Login location
     */
    void sendLoginAlertEmail(String email, String deviceInfo, String location);

    /**
     * Send generic email with template.
     *
     * @param to Recipient email
     * @param subject Email subject
     * @param templateName Template name
     * @param variables Template variables
     */
    void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables);

    /**
     * Send simple text email.
     *
     * @param to Recipient email
     * @param subject Email subject
     * @param body Email body
     */
    void sendSimpleEmail(String to, String subject, String body);
}