package com.marymar.app.controller;

import com.marymar.app.business.DTO.*;
import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.Service.AuthService;
import com.marymar.app.business.Service.PasswordRecoveryService;
import com.marymar.app.configuration.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordRecoveryService passwordRecoveryService;

    @Value("${recaptcha.site-key:}")
    private String recaptchaSiteKey;

    public AuthController(AuthService authService, PasswordRecoveryService passwordRecoveryService) {
        this.authService = authService;
        this.passwordRecoveryService = passwordRecoveryService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        if (!request.getAceptaHabeasData()) {
            throw new IllegalArgumentException("Debe aceptar la política de tratamiento de datos.");
        }

        return ResponseEntity.ok(authService.register(request));
    }

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

    @GetMapping("/mobile/google")
    public void mobileGoogleLogin(HttpServletResponse response) throws java.io.IOException {
        response.addHeader(
                "Set-Cookie",
                OAuth2SuccessHandler.OAUTH_CLIENT_COOKIE
                        + "=" + OAuth2SuccessHandler.MOBILE_CLIENT
                        + "; Max-Age=300; Path=/; SameSite=Lax"
        );
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/mobile/recaptcha/config")
    public ResponseEntity<?> mobileRecaptchaConfig() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("enabled", recaptchaSiteKey != null && !recaptchaSiteKey.isBlank());
        payload.put("siteKey", recaptchaSiteKey == null ? "" : recaptchaSiteKey);
        payload.put("provider", "google-recaptcha-v2-checkbox");
        return ResponseEntity.ok(payload);
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
}