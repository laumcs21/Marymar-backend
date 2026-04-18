package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.GoogleUserInfoDTO;
import com.marymar.app.business.Service.GoogleIdTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class GoogleIdTokenServiceImpl implements GoogleIdTokenService {

    private static final Set<String> VALID_ISSUERS = Set.of(
            "https://accounts.google.com",
            "accounts.google.com"
    );

    private final NimbusJwtDecoder jwtDecoder;
    private final String googleServerClientId;

    public GoogleIdTokenServiceImpl(
            @Value("${google.server-client-id}") String googleServerClientId
    ) {
        this.googleServerClientId = googleServerClientId != null
                ? googleServerClientId.trim()
                : "";

        this.jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .build();
    }

    @Override
    public GoogleUserInfoDTO validarToken(String idToken) {
        try {
            if (idToken == null || idToken.isBlank()) {
                throw new IllegalArgumentException("idToken de Google requerido");
            }

            if (googleServerClientId.isBlank()) {
                throw new IllegalStateException("google.server-client-id no está configurado");
            }

            Jwt jwt = jwtDecoder.decode(idToken);

            String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
            if (issuer == null || !VALID_ISSUERS.contains(issuer)) {
                throw new IllegalArgumentException("Issuer de Google no válido");
            }

            List<String> audience = jwt.getAudience();
            if (audience == null || !audience.contains(googleServerClientId)) {
                throw new IllegalArgumentException("Audience de Google no válida");
            }

            String email = jwt.getClaimAsString("email");
            String nombre = jwt.getClaimAsString("name");
            String sub = jwt.getSubject();

            Object emailVerifiedClaim = jwt.getClaims().get("email_verified");
            boolean emailVerified = false;

            if (emailVerifiedClaim instanceof Boolean) {
                emailVerified = (Boolean) emailVerifiedClaim;
            } else if (emailVerifiedClaim instanceof String) {
                emailVerified = Boolean.parseBoolean((String) emailVerifiedClaim);
            }

            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("El token de Google no contiene email");
            }

            if (!emailVerified) {
                throw new IllegalArgumentException("El email de Google no está verificado");
            }

            if (sub == null || sub.isBlank()) {
                throw new IllegalArgumentException("El token de Google no contiene subject");
            }

            if (nombre == null || nombre.isBlank()) {
                nombre = email;
            }

            return new GoogleUserInfoDTO(sub, email, nombre);

        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (JwtException e) {
            throw new IllegalArgumentException("Token de Google inválido");
        }
    }
}