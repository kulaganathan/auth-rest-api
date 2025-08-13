package com.auth.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.verification.from:noreply@authserver.com}")
    private String fromEmail;

    @Value("${app.email.verification.subject:Verify Your Email Address}")
    private String subject;

    @Value("${app.email.verification.base-url:http://localhost:3000}")
    private String baseUrl;

    public void sendVerificationEmail(String toEmail, String verificationToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        
        String verificationUrl = baseUrl + "/verify-email?token=" + verificationToken;
        String body = String.format(
            "Please click the following link to verify your email address:\n\n%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you did not create an account, please ignore this email.",
            verificationUrl
        );
        
        message.setText(body);
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");
        
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
        String body = String.format(
            "You have requested a password reset. Please click the following link to reset your password:\n\n%s\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you did not request a password reset, please ignore this email.",
            resetUrl
        );
        
        message.setText(body);
        mailSender.send(message);
    }

    public void sendWelcomeEmail(String toEmail, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to Auth Server");
        
        String body = String.format(
            "Welcome %s!\n\n" +
            "Your account has been successfully created and verified.\n\n" +
            "You can now log in to your account.\n\n" +
            "Thank you for choosing our service!",
            username
        );
        
        message.setText(body);
        mailSender.send(message);
    }

    public void sendMfaSetupEmail(String toEmail, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("MFA Setup Completed");
        
        String body = String.format(
            "Hello %s,\n\n" +
            "Multi-factor authentication has been successfully set up for your account.\n\n" +
            "Your account is now more secure. Please keep your backup codes in a safe place.\n\n" +
            "If you did not set up MFA, please contact support immediately.",
            username
        );
        
        message.setText(body);
        mailSender.send(message);
    }
}
