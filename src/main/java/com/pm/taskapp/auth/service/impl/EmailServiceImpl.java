package com.pm.taskapp.auth.service.impl;

import com.pm.taskapp.auth.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Implementation of EmailService for sending emails.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:taskapp16@gmail.com}")
    private String fromEmail;

    @Value("${app.mail.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.mail.support-email:support@taskapp.com}")
    private String supportEmail;

    @Override
    @Async
    public void sendEmailVerification(String email, String verificationToken) {
        log.info("Sending email verification to: {}", email);

        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + verificationToken;

        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("supportEmail", supportEmail);

        String subject = "Verify Your Email - TaskApp";
        String template = "email-verification"; // Template name

        // For now, using simple email (you can create Thymeleaf templates later)
        String body = String.format(
                "Hello,\n\n" +
                        "Please click the link below to verify your email address:\n\n" +
                        "%s\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "If you did not create an account, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "TaskApp Team",
                verificationUrl
        );

        sendSimpleEmail(email, subject, body);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String email, String resetToken) {
        log.info("Sending password reset email to: {}", email);

        String resetUrl = baseUrl + "/api/auth/reset-password?token=" + resetToken;

        String subject = "Password Reset Request - TaskApp";
        String body = String.format(
                "Hello,\n\n" +
                        "We received a request to reset your password.\n\n" +
                        "Please click the link below to reset your password:\n\n" +
                        "%s\n\n" +
                        "This link will expire in 1 hour.\n\n" +
                        "If you did not request a password reset, please ignore this email or contact support.\n\n" +
                        "Best regards,\n" +
                        "TaskApp Team",
                resetUrl
        );

        sendSimpleEmail(email, subject, body);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String email, String userName) {
        log.info("Sending welcome email to: {}", email);

        String subject = "Welcome to TaskApp!";
        String body = String.format(
                "Hello %s,\n\n" +
                        "Welcome to TaskApp! We're excited to have you on board.\n\n" +
                        "Your account has been successfully created. You can now log in and start managing your tasks.\n\n" +
                        "Here are some things you can do:\n" +
                        "- Create and organize your projects\n" +
                        "- Manage tasks and deadlines\n" +
                        "- Collaborate with team members\n" +
                        "- Track your progress\n\n" +
                        "If you have any questions, feel free to reach out to our support team at %s\n\n" +
                        "Best regards,\n" +
                        "TaskApp Team",
                userName,
                supportEmail
        );

        sendSimpleEmail(email, subject, body);
    }

    @Override
    @Async
    public void sendAccountLockedEmail(String email, String reason) {
        log.info("Sending account locked email to: {}", email);

        String subject = "Account Security Alert - TaskApp";
        String body = String.format(
                "Hello,\n\n" +
                        "Your account has been temporarily locked due to: %s\n\n" +
                        "This is a security measure to protect your account.\n\n" +
                        "To unlock your account, please:\n" +
                        "1. Wait 30 minutes and try again, or\n" +
                        "2. Reset your password using the 'Forgot Password' option\n" +
                        "3. Contact support at %s if you need immediate assistance\n\n" +
                        "Best regards,\n" +
                        "TaskApp Security Team",
                reason,
                supportEmail
        );

        sendSimpleEmail(email, subject, body);
    }

    @Override
    @Async
    public void sendPasswordChangedEmail(String email) {
        log.info("Sending password changed notification to: {}", email);

        String subject = "Password Changed Successfully - TaskApp";
        String body = String.format(
                "Hello,\n\n" +
                        "Your password has been changed successfully.\n\n" +
                        "If you did not make this change, please contact our support team immediately at %s\n\n" +
                        "For your security:\n" +
                        "- Never share your password with anyone\n" +
                        "- Use a strong, unique password\n" +
                        "- Enable two-factor authentication when available\n\n" +
                        "Best regards,\n" +
                        "TaskApp Security Team",
                supportEmail
        );

        sendSimpleEmail(email, subject, body);
    }

    @Override
    @Async
    public void sendLoginAlertEmail(String email, String deviceInfo, String location) {
        log.info("Sending login alert to: {}", email);

        String subject = "New Login to Your Account - TaskApp";
        String body = String.format(
                "Hello,\n\n" +
                        "We detected a new login to your account:\n\n" +
                        "Device: %s\n" +
                        "Location: %s\n" +
                        "Time: Now\n\n" +
                        "If this was you, you can safely ignore this email.\n\n" +
                        "If you don't recognize this activity, please:\n" +
                        "1. Change your password immediately\n" +
                        "2. Review your account activity\n" +
                        "3. Contact support at %s\n\n" +
                        "Best regards,\n" +
                        "TaskApp Security Team",
                deviceInfo,
                location,
                supportEmail
        );

        sendSimpleEmail(email, subject, body);
    }

    @Override
    @Async
    public void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        log.info("Sending templated email to: {} with template: {}", to, templateName);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context();
            context.setVariables(variables);

            String html = templateEngine.process(templateName, context);

            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Templated email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send templated email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        log.info("Sending simple email to: {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}