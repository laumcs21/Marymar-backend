package com.marymar.app.IntegrationTest;

import com.marymar.app.business.DTO.LoginRequestDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.DTO.RegisterRequestDTO;
import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.Exception.CredencialesInvalidasException;
import com.marymar.app.business.Service.AuthService;
import com.marymar.app.business.Service.GoogleIdTokenService;
import com.marymar.app.business.Service.RecaptchaService;
import com.marymar.app.business.Service.Util.GeneradorCodigo;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.PersonaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EntityManager entityManager;
    @MockitoBean
    private GoogleIdTokenService googleIdTokenService;

    @MockitoBean
    private RecaptchaService recaptchaService;
    @MockitoBean
    private GeneradorCodigo generadorCodigo;

    private RegisterRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDTO();
        registerRequest.setNumeroIdentificacion("123");
        registerRequest.setNombre("Laura");
        registerRequest.setEmail("laura@test.com");
        registerRequest.setContrasena("Admin123!");
        registerRequest.setTelefono("3001234567");
        registerRequest.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        registerRequest.setAceptaHabeasData(true);
        registerRequest.setCaptchaToken("captcha-ok");
        registerRequest.setRol(Rol.CLIENTE);
    }

    @Test
    void registerDeberiaPersistirUsuarioYRetornarToken() {
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(true);

        AuthResponseDTO response = authService.register(registerRequest);

        assertEquals("Laura", response.getNombre());
        assertEquals(Rol.CLIENTE, response.getRol());
        assertNotNull(response.getToken());

        Optional<Persona> personaOpt = personaRepository.findByEmail("laura@test.com");
        assertTrue(personaOpt.isPresent());
        assertNotEquals("Admin123!", personaOpt.get().getContrasena());
        assertTrue(personaOpt.get().getAceptoHabeasData());
        assertNotNull(personaOpt.get().getFechaAceptacion());
    }

    @Test
    void loginDeberiaSolicitar2FACuandoCredencialesSonValidas() {
        crearPersonaBase("laura@test.com");
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(true);

        LoginRequestDTO login = new LoginRequestDTO();
        login.setEmail("laura@test.com");
        login.setContrasena("Admin123!");
        login.setCaptchaToken("captcha-ok");

        AuthResponseDTO response = authService.login(login);

        assertTrue(response.isRequires2FA());
        assertNull(response.getToken());
        verify(generadorCodigo).generarCodigo("laura@test.com");
    }

    @Test
    void loginDeberiaBloquearCuentaAlTercerIntentoFallido() {
        Persona persona = crearPersonaBase("bloqueo@test.com");
        persona.setIntentosFallidos(2);
        personaRepository.save(persona);
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(true);

        LoginRequestDTO login = new LoginRequestDTO();
        login.setEmail("bloqueo@test.com");
        login.setContrasena("mala");
        login.setCaptchaToken("captcha-ok");

        CredencialesInvalidasException ex = assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.login(login)
        );

        assertEquals("Cuenta bloqueada por 5 minutos.", ex.getMessage());
        entityManager.flush();
        entityManager.clear();

        Persona actualizado = personaRepository.findByEmail("bloqueo@test.com").orElseThrow();
        assertEquals(0, actualizado.getIntentosFallidos());
        assertNotNull(actualizado.getBloqueadoHasta());
    }

    @Test
    void validarCodigoDeberiaRetornarJwtReal() {
        crearPersonaBase("codigo@test.com");

        AuthResponseDTO response = authService.validarCodigo("codigo@test.com", "123456");

        assertNotNull(response.getToken());
        assertFalse(response.isRequires2FA());
        verify(generadorCodigo).validarCodigo("codigo@test.com", "123456");
    }

    @Test
    void reenviarCodigoDeberiaRetornarRequires2FA() {
        crearPersonaBase("reenviar@test.com");

        AuthResponseDTO response = authService.reenviarCodigo("reenviar@test.com");

        assertTrue(response.isRequires2FA());
        assertNull(response.getToken());
        verify(generadorCodigo).generarCodigo("reenviar@test.com");
    }

    @Test
    void verifyTokenDeberiaResolverUsuarioDesdeJwt() {
        Persona persona = crearPersonaBase("token@test.com");
        String token = authService.validarCodigo("token@test.com", "654321").getToken();
        reset(generadorCodigo);

        PersonaResponseDTO response = authService.verifyToken(token);

        assertEquals(persona.getEmail(), response.getEmail());
        assertEquals(persona.getNombre(), response.getNombre());
    }

    private Persona crearPersonaBase(String email) {
        Persona persona = Persona.builder()
                .numeroIdentificacion("ID-" + email)
                .nombre("Laura")
                .email(email)
                .contrasena(passwordEncoder.encode("Admin123!"))
                .telefono("3001234567")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .rol(Rol.CLIENTE)
                .activo(true)
                .build();
        persona.setAceptoHabeasData(true);
        return personaRepository.save(persona);
    }
}