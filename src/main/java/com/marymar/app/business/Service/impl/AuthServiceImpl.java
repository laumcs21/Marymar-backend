package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.DTO.LoginRequestDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.DTO.RegisterRequestDTO;
import com.marymar.app.business.Service.AuthService;
import com.marymar.app.business.Service.PersonaService;
import com.marymar.app.configuration.Security.JwtService;
import com.marymar.app.persistence.DAO.PersonaDAO;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.PersonaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final PersonaRepository personaRepository;
    private final PersonaDAO personaDAO;
    private final PersonaService personaService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(PersonaRepository personaRepository, PersonaDAO personaDAO, PersonaService personaService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.personaRepository = personaRepository;
        this.personaService = personaService;
        this.personaDAO = personaDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

        Persona persona = personaRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getContrasena(), persona.getContrasena())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        String token = jwtService.generateToken(persona);

        return new AuthResponseDTO(
                persona.getNombre(),
                persona.getRol(),
                token
        );

    }

    @Override
    public PersonaResponseDTO verifyToken(String token) {

        jwtService.validateToken(token);

        String email = jwtService.extractUsername(token);

        return personaService.obtenerPorEmail(email);
    }
}
