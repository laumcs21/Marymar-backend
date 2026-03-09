package com.marymar.app.UnitTest;

import com.marymar.app.configuration.Security.JwtService;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceUnitTest {

    private JwtService jwtService;
    private Persona persona;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "dGVzdF9qd3Rfc2VjcmV0X2tleV90aGF0X2lzX2xvbmdfZW5vdWdoX2Zvcl9oczI1Ng==");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 60_000L);

        persona = Persona.builder()
                .numeroIdentificacion("123")
                .nombre("Laura")
                .email("laura@test.com")
                .contrasena("hash")
                .telefono("300")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .rol(Rol.ADMINISTRADOR)
                .activo(true)
                .build();
    }

    @Test
    void deberiaGenerarYExtraerClaimsDelToken() {
        String token = jwtService.generateToken(persona);

        assertEquals("laura@test.com", jwtService.extractUsername(token));
        assertEquals(Rol.ADMINISTRADOR, jwtService.extractRole(token));
        assertNotNull(jwtService.extractExpiration(token));
        assertFalse(jwtService.isTokenExpired(token));
        assertDoesNotThrow(() -> jwtService.validateToken(token));
    }

    @Test
    void deberiaValidarTokenContraUserDetails() {
        String token = jwtService.generateToken(persona);
        User userDetails = new User("laura@test.com", "hash", java.util.List.of());

        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertFalse(jwtService.isTokenValid(token, new User("otro@test.com", "hash", java.util.List.of())));
    }

    @Test
    void deberiaDetectarTokenExpirado() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1L);
        String token = jwtService.generateToken(persona);

        assertTrue(jwtService.isTokenExpired(token));
    }

    @Test
    void validateTokenDeberiaFallarSiTokenEsInvalido() {
        assertThrows(Exception.class, () -> jwtService.validateToken("token-invalido"));
    }
}
