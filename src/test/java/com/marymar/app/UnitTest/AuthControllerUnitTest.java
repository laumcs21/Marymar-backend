package com.marymar.app.UnitTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.*;
import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.Service.AuditoriaService;
import com.marymar.app.business.Service.AuthService;
import com.marymar.app.business.Service.PasswordRecoveryService;
import com.marymar.app.configuration.Security.JwtService;
import com.marymar.app.controller.AuthController;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.PersonaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock private AuthService authService;
    @Mock private AuditoriaService auditoriaService;
    @Mock private PasswordRecoveryService passwordRecoveryService;
    @Mock private JwtService jwtService;
    @Mock private PersonaRepository personaRepository;
    @Mock private HttpServletRequest httpServletRequest;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(
                authService,
                auditoriaService,
                passwordRecoveryService,
                jwtService,
                personaRepository
        );
    }

    @Test
    void registerDeberiaRetornar200YEnviarUserAgentEIp() {
        RegisterRequestDTO request = TestDataFactory.registerRequest();
        AuthResponseDTO response = new AuthResponseDTO("Laura", Rol.CLIENTE, "jwt");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("JUnit");
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");
        when(authService.register(request, "JUnit", "10.0.0.1")).thenReturn(response);

        ResponseEntity<AuthResponseDTO> resultado = controller.register(request, httpServletRequest);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
        verify(authService).register(request, "JUnit", "10.0.0.1");
    }

    @Test
    void registerDeberiaFallarSiNoAceptaHabeasData() {
        RegisterRequestDTO request = TestDataFactory.registerRequest();
        request.setAceptaHabeasData(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.register(request, httpServletRequest));

        assertEquals("Debe aceptar la política de tratamiento de datos.", ex.getMessage());
        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void loginDeberiaRetornar200SiServicioNoFalla() {
        LoginRequestDTO dto = TestDataFactory.loginRequest();
        AuthResponseDTO response = new AuthResponseDTO("Laura", Rol.CLIENTE, null, true);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("JUnit");
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("20.1.1.1");
        when(authService.login(dto, "JUnit", "20.1.1.1")).thenReturn(response);

        ResponseEntity<?> resultado = controller.login(dto, httpServletRequest);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void loginDeberiaUsarRemoteAddrSiNoHayForwardedFor() {
        LoginRequestDTO dto = TestDataFactory.loginRequest();
        AuthResponseDTO response = new AuthResponseDTO("Laura", Rol.CLIENTE, null, true);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("JUnit");
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(authService.login(dto, "JUnit", "127.0.0.1")).thenReturn(response);

        ResponseEntity<?> resultado = controller.login(dto, httpServletRequest);

        assertEquals(200, resultado.getStatusCode().value());
        verify(authService).login(dto, "JUnit", "127.0.0.1");
    }

    @Test
    void loginDeberiaRetornar401SiServicioFalla() {
        LoginRequestDTO dto = TestDataFactory.loginRequest();
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("JUnit");
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");
        when(authService.login(dto, "JUnit", "10.0.0.1"))
                .thenThrow(new RuntimeException("Credenciales inválidas"));

        ResponseEntity<?> resultado = controller.login(dto, httpServletRequest);

        assertEquals(401, resultado.getStatusCode().value());
        assertEquals("Credenciales inválidas", ((Map<?, ?>) resultado.getBody()).get("error"));
    }

    @Test
    void loginGoogleMobileDeberiaRetornar401SiServicioFalla() {
        GoogleMobileLoginRequestDTO dto = new GoogleMobileLoginRequestDTO("idToken", "captcha", "GOOGLE_LOGIN");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Android");
        when(httpServletRequest.getRemoteAddr()).thenReturn("10.2.2.2");
        when(authService.loginWithGoogleMobile(dto, "Android", "10.2.2.2"))
                .thenThrow(new RuntimeException("Captcha Android inválido"));

        ResponseEntity<?> resultado = controller.loginGoogleMobile(dto, httpServletRequest);

        assertEquals(401, resultado.getStatusCode().value());
        assertEquals("Captcha Android inválido", ((Map<?, ?>) resultado.getBody()).get("error"));
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

    @Test
    void logoutDeberiaRegistrarAuditoriaConUsuarioResueltoDesdeJwt() {
        Persona persona = TestDataFactory.persona(3L, "Laura", "laura@test.com", Rol.CLIENTE);
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer jwt-token");
        when(jwtService.extractUsername("jwt-token")).thenReturn("laura@test.com");
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));

        ResponseEntity<Void> resultado = controller.logout(httpServletRequest);

        assertEquals(200, resultado.getStatusCode().value());
        verify(auditoriaService).registrar(
                "LOGOUT",
                "USUARIO",
                3L,
                "Cierre de sesión de: laura@test.com",
                "laura@test.com"
        );
    }
}
