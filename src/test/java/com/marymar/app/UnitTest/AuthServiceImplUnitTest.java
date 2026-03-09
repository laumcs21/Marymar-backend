package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.DTO.LoginRequestDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.DTO.RegisterRequestDTO;
import com.marymar.app.business.Exception.CredencialesInvalidasException;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplUnitTest {

    @Mock
    private PersonaRepository personaRepository;
    @Mock
    private PersonaDAO personaDAO;
    @Mock
    private PersonaService personaService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private GeneradorCodigo generadorCodigo;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RecaptchaService recaptchaService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private Persona persona;

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

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("laura@test.com");
        loginRequest.setContrasena("Admin123!");
        loginRequest.setCaptchaToken("captcha-ok");

        persona = Persona.builder()
                .numeroIdentificacion("123")
                .nombre("Laura")
                .email("laura@test.com")
                .contrasena("encoded")
                .telefono("3001234567")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .rol(Rol.CLIENTE)
                .activo(true)
                .build();
    }

    @Test
    void registerDeberiaCrearUsuarioYRetornarToken() {
        when(personaDAO.existeEmail("laura@test.com")).thenReturn(false);
        when(personaDAO.existeNumeroIdentificacion("123")).thenReturn(false);
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(true);
        when(passwordEncoder.encode("Admin123!")).thenReturn("encoded");
        when(jwtService.generateToken(any(Persona.class))).thenReturn("jwt-token");

        AuthResponseDTO response = authService.register(registerRequest);

        assertEquals("Laura", response.getNombre());
        assertEquals(Rol.CLIENTE, response.getRol());
        assertEquals("jwt-token", response.getToken());
        verify(personaRepository).save(any(Persona.class));
    }

    @Test
    void registerDeberiaFallarSiEmailYaExiste() {
        when(personaDAO.existeEmail("laura@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(registerRequest));

        assertEquals("El email ya está registrado", ex.getMessage());
        verify(personaRepository, never()).save(any());
    }

    @Test
    void registerDeberiaFallarSiIdentificacionYaExiste() {
        when(personaDAO.existeEmail("laura@test.com")).thenReturn(false);
        when(personaDAO.existeNumeroIdentificacion("123")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(registerRequest));

        assertEquals("La identificación ya está registrada", ex.getMessage());
    }

    @Test
    void registerDeberiaFallarSiNoAceptaHabeasData() {
        registerRequest.setAceptaHabeasData(false);
        when(personaDAO.existeEmail(any())).thenReturn(false);
        when(personaDAO.existeNumeroIdentificacion(any())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(registerRequest));

        assertEquals("Debe aceptar la política de tratamiento de datos.", ex.getMessage());
    }

    @Test
    void registerDeberiaFallarSiCaptchaEsInvalido() {
        when(personaDAO.existeEmail(any())).thenReturn(false);
        when(personaDAO.existeNumeroIdentificacion(any())).thenReturn(false);
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(registerRequest));

        assertEquals("Captcha inválido", ex.getMessage());
    }

    @Test
    void loginDeberiaFallarSiCaptchaEsInvalido() {
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(loginRequest));

        assertEquals("Captcha inválido", ex.getMessage());
    }

    @Test
    void loginDeberiaFallarSiUsuarioNoExiste() {
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(true);
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.empty());

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(loginRequest));

        assertEquals("Correo o contraseña incorrectos", ex.getMessage());
    }

    @Test
    void loginDeberiaFallarSiCuentaEstaBloqueada() {
        persona.setBloqueadoHasta(LocalDateTime.now().plusMinutes(2));
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(true);
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(loginRequest));

        assertEquals("Cuenta bloqueada.", ex.getMessage());
    }

    @Test
    void loginExitosoDeberiaResetearIntentosYSolicitar2FA() {
        persona.setIntentosFallidos(null);
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(true);
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));

        AuthResponseDTO response = authService.login(loginRequest);

        assertEquals("Laura", response.getNombre());
        assertTrue(response.isRequires2FA());
        assertNull(response.getToken());
        assertEquals(0, persona.getIntentosFallidos());
        assertNull(persona.getBloqueadoHasta());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(personaRepository).save(persona);
        verify(generadorCodigo).generarCodigo("laura@test.com");
    }

    @Test
    void loginFallidoDeberiaIncrementarIntentosYMostrarRestantes() {
        persona.setIntentosFallidos(1);
        when(recaptchaService.validarCaptcha("captcha-ok")).thenReturn(true);
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));
        doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(loginRequest));

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
                () -> authService.login(loginRequest));

        assertEquals("Cuenta bloqueada por 5 minutos.", ex.getMessage());
        assertEquals(0, persona.getIntentosFallidos());
        assertNotNull(persona.getBloqueadoHasta());
        verify(personaRepository).save(persona);
    }

    @Test
    void verifyTokenDeberiaDelegarEnJwtYPersonaService() {
        PersonaResponseDTO dto = new PersonaResponseDTO();
        dto.setEmail("laura@test.com");
        when(personaService.obtenerPorEmail("laura@test.com")).thenReturn(dto);
        when(jwtService.extractUsername("token-ok")).thenReturn("laura@test.com");

        PersonaResponseDTO response = authService.verifyToken("token-ok");

        assertEquals("laura@test.com", response.getEmail());
        verify(jwtService).validateToken("token-ok");
    }

    @Test
    void validarCodigoDeberiaGenerarJwtCuandoUsuarioExiste() {
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));
        when(jwtService.generateToken(persona)).thenReturn("jwt-2fa");

        AuthResponseDTO response = authService.validarCodigo("laura@test.com", "123456");

        assertEquals("jwt-2fa", response.getToken());
        assertFalse(response.isRequires2FA());
        verify(generadorCodigo).validarCodigo("laura@test.com", "123456");
    }

    @Test
    void validarCodigoDeberiaFallarSiUsuarioNoExiste() {
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.validarCodigo("laura@test.com", "123456"));

        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    void reenviarCodigoDeberiaResponderConRequires2FA() {
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));

        AuthResponseDTO response = authService.reenviarCodigo("laura@test.com");

        assertEquals("Laura", response.getNombre());
        assertTrue(response.isRequires2FA());
        assertNull(response.getToken());
        verify(generadorCodigo).generarCodigo("laura@test.com");
    }

    @Test
    void reenviarCodigoDeberiaFallarSiNoSePuedeEnviar() {
        when(personaRepository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));
        doThrow(new RuntimeException("mail error")).when(generadorCodigo).generarCodigo("laura@test.com");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.reenviarCodigo("laura@test.com"));

        assertEquals("No se pudo enviar el código", ex.getMessage());
    }
}
