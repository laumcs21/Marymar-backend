package com.marymar.app.controller;

import com.marymar.app.business.DTO.*;
import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ============================
    // REGISTRO DINÁMICO POR ROL
    // ============================

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @RequestBody RegisterRequestDTO request) {

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
}
