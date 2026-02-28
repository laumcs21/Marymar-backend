package com.marymar.app.business.Service.impl;

import com.marymar.app.business.Service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sibApi.TransactionalEmailsApi;
import sendinblue.Configuration;
import sibModel.*;

import java.util.Collections;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${app.mail.from}")
    private String from;

    @Override
    public void send(String to, String code) {

        Configuration.getDefaultApiClient().setApiKey(apiKey);

        TransactionalEmailsApi api = new TransactionalEmailsApi();

        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(from);
        sender.setName("Mar y Mar");

        SendSmtpEmail email = new SendSmtpEmail();
        email.setSender(sender);
        email.setTo(Collections.singletonList(
                new SendSmtpEmailTo().email(to)
        ));
        email.setSubject("Verificación en dos pasos");
        email.setTextContent(
                "Hola,\n\n" +
                        "Tu código de inicio de sesión es: " + code + "\n" +
                        "Caduca en 10 minutos.\n\n" +
                        "Si no fuiste tú, ignora este correo."
        );

        try {
            api.sendTransacEmail(email);
            System.out.println("[MAIL] Correo enviado correctamente a " + to);
        } catch (Exception e) {
            System.err.println("[MAIL] Error enviando correo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendReset(String to, String body) {

        Configuration.getDefaultApiClient().setApiKey(apiKey);

        TransactionalEmailsApi api = new TransactionalEmailsApi();

        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(from);
        sender.setName("Mar y Mar");

        SendSmtpEmail email = new SendSmtpEmail();
        email.setSender(sender);
        email.setTo(Collections.singletonList(
                new SendSmtpEmailTo().email(to)
        ));
        email.setSubject("Recuperación de contraseña");
        email.setHtmlContent("""
                <h2>Recuperación de contraseña</h2>
                <p>Recibimos una solicitud para restablecer tu contraseña.</p>
                <p>Haz clic en el siguiente botón (válido 15 minutos):</p>
                <a href="%s" 
                   style="background-color:#f97316;color:white;padding:10px 20px;
                          text-decoration:none;border-radius:8px;">
                   Restablecer contraseña
                </a>
                <p>Si no fuiste tú, ignora este correo.</p>
            """.formatted(body));
        try {
            api.sendTransacEmail(email);
            System.out.println("[MAIL] Correo enviado correctamente a " + to);
        } catch (Exception e) {
            System.err.println("[MAIL] Error enviando correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}