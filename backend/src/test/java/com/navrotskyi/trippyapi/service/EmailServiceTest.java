package com.navrotskyi.trippyapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @Test
    void shouldSendVerificationEmailWithCorrectContent() {
        String toEmail = "john.doe@email.com";
        String token = "mock-verification-token";

        emailService.sendVerificationEmail(toEmail, token);

        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertEquals(toEmail, Objects.requireNonNull(capturedMessage.getTo())[0]);
        assertEquals("Trippy - Potwierdź swój adres email", capturedMessage.getSubject());
        assertTrue(Objects.requireNonNull(capturedMessage.getText()).contains("http://localhost:8080/api/auth/verify?token=" + token),
                "Treść maila musi zawierać poprawny link aktywacyjny");
    }
}
