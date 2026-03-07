package com.navrotskyi.trippyapi.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String confirmationUrl = "http://localhost:8080/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("kontakt@tomoiolo.pl");
        message.setTo(toEmail);
        message.setSubject("Trippy - Potwierdź swój adres email");
        message.setText("Witaj w Trippy! \n\n"
                + "Aby aktywować swoje konto i móc korzystać z aplikacji, kliknij w poniższy link:\n" 
                + confirmationUrl + "\n\n"
                + "Link jest ważny przez 24 godziny.");

        mailSender.send(message);
    }
}
