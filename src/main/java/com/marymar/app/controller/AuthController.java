package com.marymar.app.controller;

import com.marymar.app.business.DTO.*;
import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.Service.AuditoriaService;
import com.marymar.app.business.Service.AuthService;
import com.marymar.app.business.Service.PasswordRecoveryService;
import com.marymar.app.configuration.Security.JwtService;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Repository.PersonaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.marymar.app.business.Exception.CredencialesInvalidasException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuditoriaService auditoriaService;
    private final PasswordRecoveryService passwordRecoveryService;
    private final JwtService jwtService;
    private final PersonaRepository personaRepository;

    public AuthController(
            AuthService authService,
            AuditoriaService auditoriaService,
            PasswordRecoveryService passwordRecoveryService,
            JwtService jwtService,
            PersonaRepository personaRepository
    ) {
        this.authService = authService;
        this.auditoriaService = auditoriaService;
        this.passwordRecoveryService = passwordRecoveryService;
        this.jwtService = jwtService;
        this.personaRepository = personaRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @RequestBody RegisterRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        if (!request.getAceptaHabeasData()) {
            throw new IllegalArgumentException("Debe aceptar la política de tratamiento de datos.");
        }

        return ResponseEntity.ok(
                authService.register(
                        request,
                        httpRequest.getHeader("User-Agent"),
                        extractClientIp(httpRequest)
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDTO dto,
            HttpServletRequest httpRequest
    ) {
        try {
            AuthResponseDTO response = authService.login(
                    dto,
                    httpRequest.getHeader("User-Agent"),
                    extractClientIp(httpRequest)
            );
            return ResponseEntity.ok(response);

        } catch (CredencialesInvalidasException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno al iniciar sesión"));
        }
    }

    @PostMapping("/google/mobile")
    public ResponseEntity<?> loginGoogleMobile(
            @RequestBody GoogleMobileLoginRequestDTO dto,
            HttpServletRequest httpRequest
    ) {
        try {
            AuthResponseDTO response = authService.loginWithGoogleMobile(
                    dto,
                    httpRequest.getHeader("User-Agent"),
                    extractClientIp(httpRequest)
            );
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno al iniciar sesión con Google"));
        }
    }

    @PostMapping("/validate-code")
    public ResponseEntity<AuthResponseDTO> validarCodigo(
            @RequestParam String email,
            @RequestParam String code
    ) {
        return ResponseEntity.ok(authService.validarCodigo(email, code));
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestBody Map<String, String> request) {

        try {
            String token = request.get("token");

            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Token no proporcionado"));
            }

            PersonaResponseDTO user = authService.verifyToken(token);

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        AuthResponseDTO response = authService.reenviarCodigo(email);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDTO dto) {
        passwordRecoveryService.sendRecoveryEmail(dto.getEmail());
        return ResponseEntity.ok("Correo de recuperación enviado");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO dto) {

        try {
            passwordRecoveryService.resetPassword(dto.getToken(), dto.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        String email = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            email = jwtService.extractUsername(token);
        }

        Persona persona = personaRepository.findByEmail(email)
                .orElse(null);

        auditoriaService.registrar(
                "LOGOUT",
                "USUARIO",
                persona != null ? persona.getId() : null,
                "Cierre de sesión de: " + email,
                email
        );

        return ResponseEntity.ok().build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}