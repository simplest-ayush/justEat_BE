package com.justeat.backend.service.impl;

import com.justeat.backend.service.EmailService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail-from}")
    private String fromAddress;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken){

        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Reset your JustEat password");
        message.setText(
                "We received a request to reset your password.\n\n" +
                        "Click the link below to set a new password. This link expires in 15 minutes.\n\n" +
                        resetLink + "\n\n" +
                        "If you didn't request this, you can safely ignore this email."
        );

        mailSender.send(message);
        log.info("Password reset email sent to {}", toEmail);

    }
}
