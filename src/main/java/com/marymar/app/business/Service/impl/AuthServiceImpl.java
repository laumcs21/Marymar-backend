package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.DTO.LoginRequestDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.DTO.RegisterRequestDTO;
import com.marymar.app.business.Exception.CredencialesInvalidasException;
import com.marymar.app.business.Service.AuthService;
import com.marymar.app.business.Service.PersonaService;
import com.marymar.app.business.Service.RecaptchaService;
import com.marymar.app.business.Service.Util.GeneradorCodigo;
import com.marymar.app.configuration.Security.JwtService;
import com.marymar.app.persistence.DAO.PersonaDAO;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.PersonaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final PersonaRepository personaRepository;
    private final PersonaDAO personaDAO;
    private final PersonaService personaService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GeneradorCodigo generadorCodigo;
    private final AuthenticationManager authenticationManager;
    private final RecaptchaService recaptchaService;

    public AuthServiceImpl(PersonaRepository personaRepository, PersonaDAO personaDAO, PersonaService personaService, PasswordEncoder passwordEncoder, JwtService jwtService, GeneradorCodigo generadorCodigo, AuthenticationManager authenticationManager, RecaptchaService recaptchaService) {
        this.personaRepository = personaRepository;
        this.personaService = personaService;
        this.personaDAO = personaDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.generadorCodigo = generadorCodigo;
        this.authenticationManager = authenticationManager;
        this.recaptchaService = recaptchaService;
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {

        if (personaDAO.existeEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        if (personaDAO.existeNumeroIdentificacion(request.getNumeroIdentificacion())) {
            throw new IllegalArgumentException("La identificación ya está registrada");
        }

        if (!Boolean.TRUE.equals(request.getAceptaHabeasData())) {
            throw new IllegalArgumentException("Debe aceptar la política de tratamiento de datos.");
        }

        if (!recaptchaService.validarCaptcha(request.getCaptchaToken())) {
            throw new IllegalArgumentException("Captcha inválido");
        }

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

        System.out.println("TOKEN RECIBIDO BACK: " + request.getCaptchaToken());

        return new AuthResponseDTO(
                persona.getNombre(),
                persona.getRol(),
                token
                );
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        if (!recaptchaService.validarCaptcha(request.getCaptchaToken())) {
            throw new CredencialesInvalidasException("Captcha inválido");
        }
        Persona persona = personaRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos"));

        if (persona.getIntentosFallidos() == null){
            persona.setIntentosFallidos((0));
        }

        if (persona.getBloqueadoHasta() != null &&
                persona.getBloqueadoHasta().isAfter(LocalDateTime.now())) {

            throw new CredencialesInvalidasException("Cuenta bloqueada.");
        }

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getContrasena()
                    )
            );

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

        return new AuthResponseDTO(
                persona.getNombre(),
                persona.getRol(),
                null,
                true
        );
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
        System.out.println ("Email: " + email);
        System.out.println ("code: " + code);

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
