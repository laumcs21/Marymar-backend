package com.marymar.app.business.Service.impl;

import com.marymar.app.business.Service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Verificación en dos pasos");
        msg.setText(
                "Hola,\n\n" +
                        "Tu código de inicio de sesión es: " + code + "\n" +
                        "Caduca en 10 minutos.\n\n" +
                        "Si no fuiste tú, ignora este correo."
        );

        try {
            System.out.println("[MAIL] Enviando a " + to + " con code=" + code);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("[MAIL] Error enviando correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}