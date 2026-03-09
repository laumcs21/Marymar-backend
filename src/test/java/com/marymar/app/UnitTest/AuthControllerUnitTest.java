package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.DTO.*;
import com.marymar.app.business.Service.AuthService;
import com.marymar.app.business.Service.PasswordRecoveryService;
import com.marymar.app.controller.AuthController;
import com.marymar.app.persistence.Entity.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private AuthService authService;
    @Mock
    private PasswordRecoveryService passwordRecoveryService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService, passwordRecoveryService);
    }

    @Test
    void registerDeberiaRetornar200CuandoTodoEsValido() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setAceptaHabeasData(true);
        AuthResponseDTO response = new AuthResponseDTO("Laura", Rol.CLIENTE, "jwt");
        when(authService.register(request)).thenReturn(response);

        ResponseEntity<?> resultado = controller.register(request);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void registerDeberiaFallarSiNoAceptaHabeasData() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setAceptaHabeasData(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.register(request));

        assertEquals("Debe aceptar la política de tratamiento de datos.", ex.getMessage());
    }

    @Test
    void loginDeberiaRetornar200SiAuthServiceNoFalla() {
        LoginRequestDTO dto = new LoginRequestDTO();
        AuthResponseDTO response = new AuthResponseDTO("Laura", Rol.CLIENTE, null, true);
        when(authService.login(dto)).thenReturn(response);

        ResponseEntity<?> resultado = controller.login(dto);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void loginDeberiaRetornar401SiAuthServiceFalla() {
        LoginRequestDTO dto = new LoginRequestDTO();
        when(authService.login(dto)).thenThrow(new RuntimeException("Credenciales inválidas"));

        ResponseEntity<?> resultado = controller.login(dto);

        assertEquals(401, resultado.getStatusCode().value());
        assertEquals("Credenciales inválidas", ((Map<?, ?>) resultado.getBody()).get("error"));
    }

    @Test
    void validarCodigoDeberiaRetornar200() {
        AuthResponseDTO response = new AuthResponseDTO("Laura", Rol.CLIENTE, "jwt", false);
        when(authService.validarCodigo("laura@test.com", "123456")).thenReturn(response);

        ResponseEntity<AuthResponseDTO> resultado = controller.validarCodigo("laura@test.com", "123456");

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void verifyTokenDeberiaRetornar401SiFaltaToken() {
        ResponseEntity<?> resultado = controller.verifyToken(Map.of());

        assertEquals(401, resultado.getStatusCode().value());
        assertEquals("Token no proporcionado", ((Map<?, ?>) resultado.getBody()).get("error"));
    }

    @Test
    void verifyTokenDeberiaRetornarUsuarioSiTokenEsValido() {
        PersonaResponseDTO user = new PersonaResponseDTO();
        user.setEmail("laura@test.com");
        when(authService.verifyToken("jwt")).thenReturn(user);

        ResponseEntity<?> resultado = controller.verifyToken(Map.of("token", "jwt"));

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(user, resultado.getBody());
    }

    @Test
    void verifyTokenDeberiaRetornar401SiServicioFalla() {
        when(authService.verifyToken("jwt")).thenThrow(new RuntimeException("Token inválido"));

        ResponseEntity<?> resultado = controller.verifyToken(Map.of("token", "jwt"));

        assertEquals(401, resultado.getStatusCode().value());
        assertEquals("Token inválido", ((Map<?, ?>) resultado.getBody()).get("error"));
    }

    @Test
    void resendCodeDeberiaRetornar200() {
        AuthResponseDTO response = new AuthResponseDTO("Laura", Rol.CLIENTE, null, true);
        when(authService.reenviarCodigo("laura@test.com")).thenReturn(response);

        ResponseEntity<?> resultado = controller.resendCode(Map.of("email", "laura@test.com"));

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void forgotPasswordDeberiaLlamarServicioYRetornarMensaje() {
        ForgotPasswordRequestDTO dto = new ForgotPasswordRequestDTO();
        dto.setEmail("laura@test.com");

        ResponseEntity<?> resultado = controller.forgotPassword(dto);

        verify(passwordRecoveryService).sendRecoveryEmail("laura@test.com");
        assertEquals(200, resultado.getStatusCode().value());
        assertEquals("Correo de recuperación enviado", resultado.getBody());
    }

    @Test
    void resetPasswordDeberiaRetornar200SiServicioNoFalla() {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO();
        dto.setToken("token");
        dto.setNewPassword("Admin123!");

        ResponseEntity<?> resultado = controller.resetPassword(dto);

        verify(passwordRecoveryService).resetPassword("token", "Admin123!");
        assertEquals(200, resultado.getStatusCode().value());
        assertEquals("Contraseña actualizada", ((Map<?, ?>) resultado.getBody()).get("message"));
    }

    @Test
    void resetPasswordDeberiaRetornar400SiServicioFalla() {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO();
        dto.setToken("token");
        dto.setNewPassword("Admin123!");
        doThrow(new RuntimeException("Token inválido")).when(passwordRecoveryService)
                .resetPassword("token", "Admin123!");

        ResponseEntity<?> resultado = controller.resetPassword(dto);

        assertEquals(400, resultado.getStatusCode().value());
        assertEquals("Token inválido", ((Map<?, ?>) resultado.getBody()).get("message"));
    }
}
