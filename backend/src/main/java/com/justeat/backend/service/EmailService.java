package com.justeat.backend.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetToken);
}