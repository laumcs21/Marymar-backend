package com.marymar.app.business.Service.impl;


import com.marymar.app.business.Service.EmailService;
import com.marymar.app.business.Service.PasswordRecoveryService;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Repository.PersonaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import java.util.UUID;


@Service
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

    private final PersonaRepository personaRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final String FRONT_RESET_URL = "http://localhost:4200/reset-password";

    public PasswordRecoveryServiceImpl(PersonaRepository personaRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.personaRepository = personaRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void sendRecoveryEmail(String email) {

        Persona persona = personaRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con ese correo"));

        String token = UUID.randomUUID().toString();
        persona.setResetToken(token);
        persona.setResetTokenExpiration(LocalDateTime.now().plusMinutes(15));

        personaRepository.save(persona);

        String link = FRONT_RESET_URL + "?token=" + token;

        String subject = "Recuperación de contraseña";
        String body = link;

        emailService.sendReset(email, body);
    }

    @Override
    public void resetPassword(String token, String newPassword) {

        Persona persona = personaRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (persona.getResetTokenExpiration() == null ||
                persona.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        validarPassword(newPassword);
        persona.setContrasena(passwordEncoder.encode(newPassword));

        persona.setResetToken(null);
        persona.setResetTokenExpiration(null);

        personaRepository.save(persona);
    }

    private void validarPassword(String password) {

        if (password == null || password.isBlank()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }

        if (password.length() < 6) {
            throw new RuntimeException("Debe tener mínimo 6 caracteres");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("Debe contener al menos una mayúscula");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException("Debe contener al menos una minúscula");
        }

        if (!password.matches(".*\\d.*")) {
            throw new RuntimeException("Debe contener al menos un número");
        }

        if (!password.matches(".*[@$!%*?&].*")) {
            throw new RuntimeException("Debe contener al menos un carácter especial");
        }
    }
}
