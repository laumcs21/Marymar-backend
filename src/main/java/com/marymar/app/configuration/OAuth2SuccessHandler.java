package com.marymar.app.configuration;

import com.marymar.app.configuration.Security.JwtService;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.PersonaRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
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

    public static final String OAUTH_CLIENT_COOKIE = "oauth_client";
    public static final String MOBILE_CLIENT = "mobile";

    private final JwtService jwtService;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String FRONT_URL;

    @Value("${app.mobile.redirect-uri:marymar://oauth-callback}")
    private String MOBILE_REDIRECT_URI;

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
        boolean mobileLogin = isMobileLogin(request);

        if (email == null || email.isBlank()) {
            clearOAuthClientCookie(response);

            if (mobileLogin) {
                response.sendRedirect(buildMobileErrorRedirect("no_email"));
            } else {
                response.sendRedirect(FRONT_URL + "/login?error=no_email");
            }
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
        String rol = persona.getRol().name();

        clearOAuthClientCookie(response);

        String redirectUrl = mobileLogin
                ? buildMobileSuccessRedirect(token, rol)
                : buildWebSuccessRedirect(token, rol);

        response.sendRedirect(redirectUrl);
    }

    private boolean isMobileLogin(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return false;
        }

        for (Cookie cookie : request.getCookies()) {
            if (OAUTH_CLIENT_COOKIE.equals(cookie.getName()) && MOBILE_CLIENT.equals(cookie.getValue())) {
                return true;
            }
        }
        return false;
    }

    private String buildWebSuccessRedirect(String token, String rol) {
        return UriComponentsBuilder
                .fromUriString(FRONT_URL + "/oauth-callback")
                .fragment("token=" + token + "&rol=" + rol)
                .build()
                .toUriString();
    }

    private String buildMobileSuccessRedirect(String token, String rol) {
        return UriComponentsBuilder
                .fromUriString(MOBILE_REDIRECT_URI)
                .fragment("token=" + token + "&rol=" + rol)
                .build()
                .toUriString();
    }

    private String buildMobileErrorRedirect(String errorCode) {
        return UriComponentsBuilder
                .fromUriString(MOBILE_REDIRECT_URI)
                .fragment("error=" + errorCode)
                .build()
                .toUriString();
    }

    public static void clearOAuthClientCookie(HttpServletResponse response) {
        response.addHeader(
                "Set-Cookie",
                OAUTH_CLIENT_COOKIE + "=; Max-Age=0; Path=/; SameSite=Lax"
        );
    }
}