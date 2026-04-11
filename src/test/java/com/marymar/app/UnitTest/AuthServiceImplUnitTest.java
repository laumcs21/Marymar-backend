package com.marymar.app.UnitTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.*;
import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.Exception.CredencialesInvalidasException;
import com.marymar.app.business.Service.AndroidRecaptchaService;
import com.marymar.app.business.Service.AuditoriaService;
import com.marymar.app.business.Service.GoogleIdTokenService;
import com.marymar.app.business.Service.PersonaService;
import com.marymar.app.business.Service.RecaptchaService;
import com.marymar.app.business.Service.Util.GeneradorCodigo;
import com.marymar.app.business.Service.impl.AuthServiceImpl;
import com.marymar.app.configuration.Security.JwtService;
import com.marymar.app.persistence.DAO.PersonaDAO;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplUnitTest {

    @Mock private PersonaRepository personaRepository;
    @Mock private PersonaDAO personaDAO;
    @Mock private PersonaService personaService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private GeneradorCodigo generadorCodigo;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RecaptchaService recaptchaService;
    @Mock private AndroidRecaptchaService androidRecaptchaService;
    @Mock private AuditoriaService auditoriaService;
    @Mock private GoogleIdTokenService googleIdTokenService;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private Persona persona;

    @BeforeEach
    void setUp() {
        registerRequest = TestDataFactory.registerRequest();
        loginRequest = TestDataFactory.loginRequest();
        persona = TestDataFactory.persona(1L, "Laura", "laura@test.com", Rol.CLIENTE);
        persona.setContrasena("encoded");
    }

    @Test
    void registerDeberiaCrearUsuarioYRetornarToken() {
        when(personaDAO.existeEmail("laura@test.com")).thenReturn(false);
        when(personaDAO.existeNumeroIdentificacion("123456789")).thenReturn(false);
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(true);
        when(passwordEncoder.encode("Admin123$")).thenReturn("encoded-pass");
        when(jwtService.generateToken(any(Persona.class))).thenReturn("jwt-token");

        AuthResponseDTO response = authService.register(registerRequest, "JUnit", "10.0.0.1");

        assertEquals("Laura", response.getNombre());
        assertEquals(Rol.CLIENTE, response.getRol());
        assertEquals("jwt-token", response.getToken());
        verify(personaRepository).save(any(Persona.class));
        verify(recaptchaService).validarCaptcha("captcha-ok");
    }

    @Test
    void registerDeberiaUsarCaptchaAndroidCuandoClienteEsAndroid() {
        registerRequest.setCaptchaClient("ANDROID");
        registerRequest.setCaptchaAction("REGISTER");
        when(personaDAO.existeEmail(any())).thenReturn(false);
        when(personaDAO.existeNumeroIdentificacion(any())).thenReturn(false);
        when(androidRecaptchaService.validarCaptchaAndroid("captcha-ok", "REGISTER", "Android", "1.1.1.1"))
                .thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("encoded-pass");
        when(jwtService.generateToken(any(Persona.class))).thenReturn("jwt");

        AuthResponseDTO response = authService.register(registerRequest, "Android", "1.1.1.1");

        assertEquals("jwt", response.getToken());
        verify(androidRecaptchaService).validarCaptchaAndroid("captcha-ok", "REGISTER", "Android", "1.1.1.1");
        verify(recaptchaService, never()).validarCaptcha(any());
    }

    @Test
    void registerDeberiaPermitirCaptchaDePruebaSinValidarServiciosExternos() {
        registerRequest.setCaptchaToken("test-captcha");
        when(personaDAO.existeEmail(any())).thenReturn(false);
        when(personaDAO.existeNumeroIdentificacion(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded-pass");
        when(jwtService.generateToken(any(Persona.class))).thenReturn("jwt");

        AuthResponseDTO response = authService.register(registerRequest, "JUnit", "127.0.0.1");

        assertEquals("jwt", response.getToken());
        verify(recaptchaService, never()).validarCaptcha(any());
        verify(androidRecaptchaService, never()).validarCaptchaAndroid(any(), any(), any(), any());
    }

    @Test
    void registerDeberiaFallarSiEmailYaExiste() {
        when(personaDAO.existeEmail("laura@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(registerRequest, null, null));

        assertEquals("El email ya está registrado", ex.getMessage());
        verify(personaRepository, never()).save(any());
    }

    @Test
    void registerDeberiaFallarSiCaptchaAndroidNoTieneAccion() {
        registerRequest.setCaptchaClient("ANDROID");
        registerRequest.setCaptchaAction(null);
        when(personaDAO.existeEmail(any())).thenReturn(false);
        when(personaDAO.existeNumeroIdentificacion(any())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(registerRequest, "Android", "1.1.1.1"));

        assertEquals("captchaAction es requerido para Android", ex.getMessage());
    }

    @Test
    void loginExitosoDeberiaResetearIntentosGenerarCodigoYRegistrarAuditoria() {
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(true);
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        AuthResponseDTO response = authService.login(loginRequest, "JUnit", "10.0.0.1");

        assertEquals("Laura", response.getNombre());
        assertTrue(response.isRequires2FA());
        assertNull(response.getToken());
        assertEquals(0, persona.getIntentosFallidos());
        assertNull(persona.getBloqueadoHasta());
        verify(generadorCodigo).generarCodigo("laura@test.com");
        verify(auditoriaService).registrar("LOGIN", "USUARIO", 1L, "Inicio de sesión", null);
    }

    @Test
    void loginDeberiaFallarSiCuentaEstaBloqueada() {
        persona.setBloqueadoHasta(LocalDateTime.now().plusMinutes(2));
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(true);
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(loginRequest, null, null));

        assertEquals("Cuenta bloqueada.", ex.getMessage());
    }

    @Test
    void loginFallidoDeberiaIncrementarIntentosYMostrarRestantes() {
        persona.setIntentosFallidos(1);
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(true);
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));
        doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(loginRequest, null, null));

        assertEquals(2, persona.getIntentosFallidos());
        assertTrue(ex.getMessage().contains("Te quedan 1 intentos"));
        verify(personaRepository).save(persona);
    }

    @Test
    void loginFallidoDeberiaBloquearCuentaAlTercerIntento() {
        persona.setIntentosFallidos(2);
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(true);
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));
        doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(loginRequest, null, null));

        assertEquals("Cuenta bloqueada por 5 minutos.", ex.getMessage());
        assertEquals(0, persona.getIntentosFallidos());
        assertNotNull(persona.getBloqueadoHasta());
    }

    @Test
    void loginConCaptchaAndroidInvalidoDeberiaFallar() {
        loginRequest.setCaptchaClient("ANDROID");
        loginRequest.setCaptchaAction("LOGIN");
        when(androidRecaptchaService.validarCaptchaAndroid("captcha-ok", "LOGIN", "Android", "1.1.1.1"))
                .thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(loginRequest, "Android", "1.1.1.1"));

        assertEquals("Captcha inválido", ex.getMessage());
    }

    @Test
    void loginWithGoogleMobileDeberiaCrearUsuarioSiNoExisteYRetornarJwt() {
        GoogleMobileLoginRequestDTO request = new GoogleMobileLoginRequestDTO("google-id-token", "captcha-android", "GOOGLE_LOGIN");
        GoogleUserInfoDTO googleUser = new GoogleUserInfoDTO("sub123", "google@test.com", "Google User");

        when(androidRecaptchaService.validarCaptchaAndroid("captcha-android", "GOOGLE_LOGIN", "Android", "10.0.0.5"))
                .thenReturn(true);
        when(googleIdTokenService.validarToken("google-id-token")).thenReturn(googleUser);
        when(personaRepository.findByEmail("google@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded-random");
        when(personaRepository.save(any(Persona.class))).thenAnswer(invocation -> {
            Persona creada = invocation.getArgument(0);
            TestDataFactory.setField(creada, "id", 99L);
            return creada;
        });
        when(jwtService.generateToken(any(Persona.class))).thenReturn("jwt-google");

        AuthResponseDTO response = authService.loginWithGoogleMobile(request, "Android", "10.0.0.5");

        assertEquals("Google User", response.getNombre());
        assertEquals(Rol.CLIENTE, response.getRol());
        assertEquals("jwt-google", response.getToken());
        assertFalse(response.isRequires2FA());
        verify(auditoriaService).registrar(
                "LOGIN_GOOGLE_MOBILE",
                "USUARIO",
                99L,
                "Inicio de sesión con Google desde app móvil",
                "google@test.com"
        );
    }

    @Test
    void loginWithGoogleMobileDeberiaFallarSiFaltaCaptchaAction() {
        GoogleMobileLoginRequestDTO request = new GoogleMobileLoginRequestDTO();
        request.setIdToken("google-id-token");
        request.setCaptchaToken("captcha");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.loginWithGoogleMobile(request, "Android", "1.1.1.1"));

        assertEquals("captchaAction es requerido", ex.getMessage());
    }

    @Test
    void verifyTokenDeberiaDelegarEnJwtYPersonaService() {
        PersonaResponseDTO dto = new PersonaResponseDTO();
        dto.setEmail("laura@test.com");
        when(jwtService.extractUsername("jwt")).thenReturn("laura@test.com");
        when(personaService.obtenerPorEmail("laura@test.com")).thenReturn(dto);

        PersonaResponseDTO resultado = authService.verifyToken("jwt");

        assertSame(dto, resultado);
        verify(jwtService).validateToken("jwt");
    }

    @Test
    void validarCodigoDeberiaGenerarJwtReal() {
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));
        when(jwtService.generateToken(persona)).thenReturn("jwt-2fa");

        AuthResponseDTO response = authService.validarCodigo("laura@test.com", "123456");

        assertEquals("jwt-2fa", response.getToken());
        assertFalse(response.isRequires2FA());
        verify(generadorCodigo).validarCodigo("laura@test.com", "123456");
    }

    @Test
    void reenviarCodigoDeberiaRetornarRequires2FA() {
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));

        AuthResponseDTO response = authService.reenviarCodigo("laura@test.com");

        assertTrue(response.isRequires2FA());
        assertNull(response.getToken());
        verify(generadorCodigo).generarCodigo("laura@test.com");
    }
}
