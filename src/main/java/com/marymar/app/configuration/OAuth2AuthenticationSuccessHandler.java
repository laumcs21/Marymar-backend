package com.marymar.app.configuration;

import com.marymar.app.business.Service.PersonaService;
import com.marymar.app.configuration.Security.JwtService;
import com.marymar.app.persistence.Entity.Persona;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final PersonaService personaService;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService,
                                              PersonaService personaService) {
        this.jwtService = jwtService;
        this.personaService = personaService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String nombre = oAuth2User.getAttribute("name");

        Persona persona = personaService.buscarOCrearUsuarioGoogle(email, nombre);

        String token = jwtService.generateToken(persona);

        response.setContentType("application/json");
        response.getWriter().write("{\"token\":\"" + token + "\"}");
        response.getWriter().flush();    }
}

