package com.marymar.app.UnitTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.Service.EmailService;
import com.marymar.app.business.Service.impl.PasswordRecoveryServiceImpl;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceImplUnitTest {

    @Mock private PersonaRepository personaRepository;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private PasswordRecoveryServiceImpl service;

    private Persona persona;

    @BeforeEach
    void setUp() {
        persona = TestDataFactory.persona(1L, "Laura", "laura@test.com", Rol.CLIENTE);
        persona.setContrasena("vieja");
        com.marymar.app.TestSupport.TestDataFactory.setField(service, "FRONT_RESET_URL", "http://localhost:4200");
    }

    @Test
    void sendRecoveryEmailDeberiaGenerarTokenYPersistirYEnviarCorreo() {
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));

        service.sendRecoveryEmail("laura@test.com");

        assertNotNull(persona.getResetToken());
        assertNotNull(persona.getResetTokenExpiration());
        verify(personaRepository).save(persona);
        verify(emailService).sendReset(eq("laura@test.com"), contains("/reset-password?token="));
    }

    @Test
    void resetPasswordDeberiaFallarSiTokenExpirado() {
        persona.setResetToken("token");
        persona.setResetTokenExpiration(LocalDateTime.now().minusMinutes(1));
        when(personaRepository.findByResetToken("token")).thenReturn(Optional.of(persona));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.resetPassword("token", "Abc123$"));

        assertEquals("Token expirado", ex.getMessage());
    }

    @Test
    void resetPasswordDeberiaActualizarContrasenaYLimpiarToken() {
        persona.setResetToken("token");
        persona.setResetTokenExpiration(LocalDateTime.now().plusMinutes(10));
        when(personaRepository.findByResetToken("token")).thenReturn(Optional.of(persona));
        when(passwordEncoder.encode("Abc123$")).thenReturn("encoded");

        service.resetPassword("token", "Abc123$");

        assertEquals("encoded", persona.getContrasena());
        assertNull(persona.getResetToken());
        assertNull(persona.getResetTokenExpiration());
        verify(personaRepository).save(persona);
    }
}
