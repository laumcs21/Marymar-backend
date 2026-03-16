package com.marymar.app.business.Service.impl;

import com.marymar.app.business.Service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import sibApi.TransactionalEmailsApi;
import sendinblue.Configuration;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${app.mail.from}")
    private String from;

    private String loadTemplate(String name) {
        try {
            ClassPathResource resource = new ClassPathResource("email/" + name);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error cargando plantilla", e);
        }
    }

    @Override
    public void send(String to, String code) {
        System.out.println("Entró A SEND VERIFICACION");
        Configuration.getDefaultApiClient().setApiKey(apiKey);

        TransactionalEmailsApi api = new TransactionalEmailsApi();

        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(from);
        sender.setName("Mar y Mar");

        String template = loadTemplate("verificacion.html");
        String html = template.replace("{{CODE}}", code);

        SendSmtpEmail email = new SendSmtpEmail();
        email.setSender(sender);
        email.setTo(Collections.singletonList(
                new SendSmtpEmailTo().email(to)
        ));
        email.setSubject("Verificación en dos pasos");
        email.setHtmlContent(html);

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

        String template = loadTemplate("recuperacion.html");
        String html = template.replace("{{LINK}}", body);

        SendSmtpEmail email = new SendSmtpEmail();
        email.setSender(sender);
        email.setTo(Collections.singletonList(
                new SendSmtpEmailTo().email(to)
        ));
        email.setSubject("Recuperación de contraseña");
        email.setHtmlContent(html);

        try {
            api.sendTransacEmail(email);
            System.out.println("[MAIL] Correo enviado correctamente a " + to);
        } catch (Exception e) {
            System.err.println("[MAIL] Error enviando correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}