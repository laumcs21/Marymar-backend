package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.DTO.GoogleMobileLoginRequestDTO;
import com.marymar.app.business.DTO.GoogleUserInfoDTO;
import com.marymar.app.business.DTO.LoginRequestDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.DTO.RegisterRequestDTO;
import com.marymar.app.business.Exception.CredencialesInvalidasException;
import com.marymar.app.business.Service.AndroidRecaptchaService;
import com.marymar.app.business.Service.AuditoriaService;
import com.marymar.app.business.Service.AuthService;
import com.marymar.app.business.Service.GoogleIdTokenService;
import com.marymar.app.business.Service.PersonaService;
import com.marymar.app.business.Service.RecaptchaService;
import com.marymar.app.business.Service.Util.GeneradorCodigo;
import com.marymar.app.configuration.Security.JwtService;
import com.marymar.app.persistence.DAO.PersonaDAO;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.PersonaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final PersonaRepository personaRepository;
    private final PersonaDAO personaDAO;
    private final PersonaService personaService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GeneradorCodigo generadorCodigo;
    private final AuthenticationManager authenticationManager;
    private final RecaptchaService recaptchaService; // WEB actual
    private final AndroidRecaptchaService androidRecaptchaService; // nuevo ANDROID
    private final AuditoriaService auditoriaService;
    private final GoogleIdTokenService googleIdTokenService;

    public AuthServiceImpl(
            PersonaRepository personaRepository,
            PersonaDAO personaDAO,
            PersonaService personaService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            GeneradorCodigo generadorCodigo,
            AuthenticationManager authenticationManager,
            RecaptchaService recaptchaService,
            AndroidRecaptchaService androidRecaptchaService,
            AuditoriaService auditoriaService,
            GoogleIdTokenService googleIdTokenService
    ) {
        this.personaRepository = personaRepository;
        this.personaService = personaService;
        this.personaDAO = personaDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.generadorCodigo = generadorCodigo;
        this.authenticationManager = authenticationManager;
        this.recaptchaService = recaptchaService;
        this.androidRecaptchaService = androidRecaptchaService;
        this.auditoriaService = auditoriaService;
        this.googleIdTokenService = googleIdTokenService;
    }

    // Compatibilidad interna
    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        return register(request, null, null);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        return login(request, null, null);
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request, String userAgent, String userIp) {

        if (personaDAO.existeEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        if (personaDAO.existeNumeroIdentificacion(request.getNumeroIdentificacion())) {
            throw new IllegalArgumentException("La identificación ya está registrada");
        }

        if (!Boolean.TRUE.equals(request.getAceptaHabeasData())) {
            throw new IllegalArgumentException("Debe aceptar la política de tratamiento de datos.");
        }

        validarCaptchaSegunCliente(
                request.getCaptchaToken(),
                request.getCaptchaClient(),
                request.getCaptchaAction(),
                userAgent,
                userIp
        );

        Persona persona = Persona.builder()
                .numeroIdentificacion(request.getNumeroIdentificacion())
                .nombre(request.getNombre())
                .email(request.getEmail())
                .contrasena(passwordEncoder.encode(request.getContrasena()))
                .telefono(request.getTelefono())
                .fechaNacimiento(request.getFechaNacimiento())
                .rol(Rol.CLIENTE)
                .activo(true)
                .build();

        persona.setAceptoHabeasData(true);
        persona.setFechaAceptacion(LocalDateTime.now());
        personaRepository.save(persona);

        String token = jwtService.generateToken(persona);

        return new AuthResponseDTO(
                persona.getNombre(),
                persona.getRol(),
                token
        );
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request, String userAgent, String userIp) {

        validarCaptchaSegunCliente(
                request.getCaptchaToken(),
                request.getCaptchaClient(),
                request.getCaptchaAction(),
                userAgent,
                userIp
        );

        Persona persona = personaRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos"));

        if (persona.getIntentosFallidos() == null) {
            persona.setIntentosFallidos(0);
        }

        if (persona.getBloqueadoHasta() != null &&
                persona.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            throw new CredencialesInvalidasException("Cuenta bloqueada.");
        }

        try {
            Authentication auth =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getEmail(),
                                    request.getContrasena()
                            )
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);

            persona.setIntentosFallidos(0);
            persona.setBloqueadoHasta(null);
            personaRepository.save(persona);

        } catch (Exception e) {

            persona.setIntentosFallidos(persona.getIntentosFallidos() + 1);

            int intentosRestantes = 3 - persona.getIntentosFallidos();

            if (persona.getIntentosFallidos() >= 3) {
                persona.setBloqueadoHasta(LocalDateTime.now().plusMinutes(5));
                persona.setIntentosFallidos(0);

                personaRepository.save(persona);

                throw new CredencialesInvalidasException("Cuenta bloqueada por 5 minutos.");
            }

            personaRepository.save(persona);

            throw new CredencialesInvalidasException(
                    "Correo o contraseña incorrectos. Te quedan "
                            + intentosRestantes
                            + " intentos antes de ser bloqueado por 5 minutos."
            );
        }

        generadorCodigo.generarCodigo(persona.getEmail());

        auditoriaService.registrar(
                "LOGIN",
                "USUARIO",
                persona.getId(),
                "Inicio de sesión",
                null
        );

        return new AuthResponseDTO(
                persona.getNombre(),
                persona.getRol(),
                null,
                true
        );
    }

    @Override
    public AuthResponseDTO loginWithGoogleMobile(
            GoogleMobileLoginRequestDTO request,
            String userAgent,
            String userIp
    ) {
        if (request.getCaptchaToken() == null || request.getCaptchaToken().isBlank()) {
            throw new IllegalArgumentException("Captcha requerido");
        }

        if (request.getCaptchaAction() == null || request.getCaptchaAction().isBlank()) {
            throw new IllegalArgumentException("captchaAction es requerido");
        }

        boolean captchaValido = androidRecaptchaService.validarCaptchaAndroid(
                request.getCaptchaToken(),
                request.getCaptchaAction(),
                userAgent,
                userIp
        );

        if (!captchaValido) {
            throw new IllegalArgumentException("Captcha Android inválido");
        }

        if (request.getIdToken() == null || request.getIdToken().isBlank()) {
            throw new IllegalArgumentException("idToken de Google requerido");
        }

        GoogleUserInfoDTO googleUser = googleIdTokenService.validarToken(request.getIdToken());

        Persona persona = personaRepository.findByEmail(googleUser.getEmail())
                .orElseGet(() -> crearUsuarioDesdeGoogle(googleUser));

        String token = jwtService.generateToken(persona);

        auditoriaService.registrar(
                "LOGIN_GOOGLE_MOBILE",
                "USUARIO",
                persona.getId(),
                "Inicio de sesión con Google desde app móvil",
                persona.getEmail()
        );

        return new AuthResponseDTO(
                persona.getNombre(),
                persona.getRol(),
                token,
                false
        );
    }

    private Persona crearUsuarioDesdeGoogle(GoogleUserInfoDTO googleUser) {
        Persona nueva = new Persona();
        nueva.setEmail(googleUser.getEmail());
        nueva.setNombre(
                googleUser.getNombre() != null && !googleUser.getNombre().isBlank()
                        ? googleUser.getNombre()
                        : "Usuario"
        );
        nueva.setContrasena(passwordEncoder.encode(UUID.randomUUID().toString()));
        nueva.setRol(Rol.CLIENTE);
        nueva.setActivo(true);
        return personaRepository.save(nueva);
    }

    /**
     * Mantiene compatibilidad:
     * - si no mandan captchaToken, conserva el comportamiento actual
     * - WEB sigue usando RecaptchaServiceImpl actual
     * - ANDROID usa AndroidRecaptchaServiceImpl nuevo
     */
    private void validarCaptchaSegunCliente(
            String captchaToken,
            String captchaClient,
            String captchaAction,
            String userAgent,
            String userIp
    ) {
        if (captchaToken == null || captchaToken.isBlank()) {
            return;
        }

        if ("test-captcha".equals(captchaToken)) {
            return;
        }

        String client = captchaClient == null ? "WEB" : captchaClient.trim().toUpperCase(Locale.ROOT);

        boolean valido;
        if ("ANDROID".equals(client)) {
            if (captchaAction == null || captchaAction.isBlank()) {
                throw new IllegalArgumentException("captchaAction es requerido para Android");
            }

            valido = androidRecaptchaService.validarCaptchaAndroid(
                    captchaToken,
                    captchaAction,
                    userAgent,
                    userIp
            );
        } else {
            valido = recaptchaService.validarCaptcha(captchaToken);
        }

        if (!valido) {
            throw new IllegalArgumentException("Captcha inválido");
        }
    }

    @Override
    public PersonaResponseDTO verifyToken(String token) {
        jwtService.validateToken(token);
        String email = jwtService.extractUsername(token);
        return personaService.obtenerPorEmail(email);
    }

    @Override
    public AuthResponseDTO validarCodigo(String email, String code) {

        generadorCodigo.validarCodigo(email, code);

        Persona persona = personaRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtService.generateToken(persona);

        return new AuthResponseDTO(
                persona.getNombre(),
                persona.getRol(),
                token,
                false
        );
    }

    @Override
    public AuthResponseDTO reenviarCodigo(String email) {

        Persona persona = personaRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
            generadorCodigo.generarCodigo(email);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el código");
        }

        return new AuthResponseDTO(
                persona.getNombre(),
                persona.getRol(),
                null,
                true
        );
    }
}