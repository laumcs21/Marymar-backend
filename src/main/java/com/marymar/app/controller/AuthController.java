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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuditoriaService auditoriaService;
    private final PasswordRecoveryService passwordRecoveryService;
    private final JwtService jwtService;
    private final PersonaRepository personaRepository;

    public AuthController(AuthService authService, AuditoriaService auditoriaService, PasswordRecoveryService passwordRecoveryService, JwtService jwtService, PersonaRepository personaRepository) {
        this.authService = authService;
        this.auditoriaService = auditoriaService;
        this.passwordRecoveryService = passwordRecoveryService;
        this.jwtService = jwtService;
        this.personaRepository = personaRepository;
    }

    // ============================
    // REGISTRO DINÁMICO POR ROL
    // ============================

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {

        if (!request.getAceptaHabeasData()) {
            throw new IllegalArgumentException("Debe aceptar la política de tratamiento de datos.");
        }

        return ResponseEntity.ok(authService.register(request));
    }

    // ============================
    // LOGIN
    // ============================

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {

        try {
            AuthResponseDTO response = authService.login(dto);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============================
    // VALIDAR CÓDIGO
    // ============================
    @PostMapping("/validate-code")
    public ResponseEntity<AuthResponseDTO> validarCodigo(
            @RequestParam String email,
            @RequestParam String code
    ) {
        return ResponseEntity.ok(authService.validarCodigo(email, code));
    }

    // ============================
    // VERIFY TOKEN (para JWT luego)
    // ============================

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
}
