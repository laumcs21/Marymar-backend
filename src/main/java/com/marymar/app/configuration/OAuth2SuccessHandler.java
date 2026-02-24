package com.marymar.app.configuration;

import com.marymar.app.configuration.Security.JwtService;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.PersonaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;

    private final String FRONT_URL = "http://localhost:4200/oauth-callback";

    public OAuth2SuccessHandler(
            JwtService jwtService,
            PersonaRepository personaRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtService = jwtService;
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String nombre = oAuth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            response.sendRedirect("http://localhost:4200/login?error=no_email");
            return;
        }

        Persona persona = personaRepository.findByEmail(email)
                .orElseGet(() -> {
                    String fakePassword = passwordEncoder.encode(UUID.randomUUID().toString());
                    Persona nueva = new Persona();
                    nueva.setEmail(email);
                    nueva.setNombre(nombre != null ? nombre : "Usuario");
                    nueva.setContrasena(fakePassword);
                    nueva.setRol(Rol.CLIENTE);
                    nueva.setActivo(true);
                    return personaRepository.save(nueva);
                });

        String token = jwtService.generateToken(persona);
        String rol = persona.getRol().name(); // "CLIENTE", "ADMINISTRADOR", "MESERO"

        String redirectUrl = UriComponentsBuilder
                .fromUriString(FRONT_URL)
                .fragment("token=" + token + "&rol=" + rol)
                .build()
                .toUriString();

        System.out.println("ATTRIBUTES: " + oAuth2User.getAttributes());
        response.sendRedirect(redirectUrl);
    }
}