package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.DTO.LoginRequestDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.DTO.RegisterRequestDTO;
import com.marymar.app.business.Service.AuthService;
import com.marymar.app.business.Service.PersonaService;
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
import org.springframework.security.core.Authentication;

@Service
public class AuthServiceImpl implements AuthService {

    private final PersonaRepository personaRepository;
    private final PersonaDAO personaDAO;
    private final PersonaService personaService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GeneradorCodigo generadorCodigo;
    private final AuthenticationManager authenticationManager;


    public AuthServiceImpl(PersonaRepository personaRepository, PersonaDAO personaDAO, PersonaService personaService, PasswordEncoder passwordEncoder, JwtService jwtService, GeneradorCodigo generadorCodigo, AuthenticationManager authenticationManager) {
        this.personaRepository = personaRepository;
        this.personaService = personaService;
        this.personaDAO = personaDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.generadorCodigo = generadorCodigo;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {

        if (personaDAO.existeEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Rol rol;

        Persona persona = Persona.builder()
                .numeroIdentificacion(request.getNumeroIdentificacion())
                .nombre(request.getNombre())
                .email(request.getEmail())
                .contrasena(passwordEncoder.encode(request.getContrasena()))
                .telefono(request.getTelefono())
                .fechaNacimiento(request.getFechaNacimiento())
                .rol(request.getRol())
                .activo(true)
                .build();

        personaRepository.save(persona);

        String token = jwtService.generateToken(persona);

        return new AuthResponseDTO(
                persona.getNombre(),
                persona.getRol(),
                token
                );
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getContrasena()
                )
        );

        Persona persona = personaRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

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

        generadorCodigo.generarCodigo(email);

        return new AuthResponseDTO(
                persona.getNombre(),
                persona.getRol(),
                null,
                true
        );
    }

}
