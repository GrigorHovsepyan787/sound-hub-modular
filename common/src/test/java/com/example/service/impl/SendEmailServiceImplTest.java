package com.example.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendEmailServiceImplTest {

    @Mock private JavaMailSender emailSender;
    @Mock private TemplateEngine templateEngine;
    @Mock private MimeMessage mimeMessage;

    private SendEmailServiceImpl sendEmailService;

    @BeforeEach
    void setUp() {
        sendEmailService = new SendEmailServiceImpl(emailSender, templateEngine);
    }

    @Test
    void sendMail_happyPath_sendsSimplelMailMessage() {
        sendEmailService.sendMail("user@example.com", "Subject", "Content");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(emailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("user@example.com");
        assertThat(sent.getSubject()).isEqualTo("Subject");
        assertThat(sent.getText()).isEqualTo("Content");
    }

    @Test
    void sendMail_withNullContent_sendsNullContent() {
        sendEmailService.sendMail("user@example.com", "Subject", null);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(emailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).isNull();
    }

    @Test
    void sendVerificationMail_happyPath_sendsHtmlEmailWithVerificationCode() throws MessagingException {
        String to = "user@example.com";
        String code = "123456";
        Locale locale = Locale.ENGLISH;
        String html = "<html>123456</html>";

        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("mailVerificationTemplate"), any(Context.class))).thenReturn(html);

        sendEmailService.sendVerificationMail(to, code, locale);

        verify(emailSender).send(mimeMessage);
        verify(templateEngine).process(eq("mailVerificationTemplate"), any(Context.class));
    }

    @Test
    void sendVerificationMail_templateEngineProcessed_withCorrectCode() throws MessagingException {
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("mailVerificationTemplate"), any(Context.class))).thenReturn("<html/>");

        sendEmailService.sendVerificationMail("user@example.com", "654321", Locale.ENGLISH);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("mailVerificationTemplate"), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getVariable("verificationCode")).isEqualTo("654321");
    }

    @Test
    void sendVerificationMail_withDifferentLocale_passesLocaleToContext() throws MessagingException {
        Locale locale = Locale.FRENCH;
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("mailVerificationTemplate"), any(Context.class))).thenReturn("<html/>");

        sendEmailService.sendVerificationMail("user@example.com", "111111", locale);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("mailVerificationTemplate"), contextCaptor.capture());
        assertThat(contextCaptor.getValue().getLocale()).isEqualTo(locale);
    }
}