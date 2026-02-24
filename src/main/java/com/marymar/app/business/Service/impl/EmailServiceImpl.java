package com.marymar.app.business.Service.impl;

import com.marymar.app.business.Service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sibApi.TransactionalEmailsApi;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.*;import sibModel.*;

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
        sender.setName("MaryMar");

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
}