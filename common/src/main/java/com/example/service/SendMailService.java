package com.example.service;

import jakarta.mail.MessagingException;

import java.util.Locale;

public interface SendMailService {
    void sendMail(String to, String subject, String content);

    void sendVerificationMail(String to, String verificationCode, Locale locale) throws MessagingException;
}
