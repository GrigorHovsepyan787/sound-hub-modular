package com.example.service.impl;

import com.example.service.SendMailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

@Service
public class SendEmailServiceImpl implements SendMailService {
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    public SendEmailServiceImpl(JavaMailSender emailSender,
                                @Qualifier("emailTemplateEngine") TemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    @Async
    public void sendMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        emailSender.send(message);
    }

    @Override
    @Async
    public void sendVerificationMail(String to, String verificationCode, Locale locale) throws MessagingException {
        final Context context = new Context(locale);
        context.setVariable("verificationCode", verificationCode);
        final MimeMessage message = emailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setSubject("Email verification code");
        helper.setFrom("soundhubnoreply@gmail.com");
        helper.setTo(to);
        final String html = templateEngine.process("mailVerificationTemplate", context);
        helper.setText(html, true);
        emailSender.send(message);
    }
}
