package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.LoginRequestDTO;
import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.DTO.RegisterRequestDTO;

public interface AuthService {

        AuthResponseDTO register(RegisterRequestDTO request);

        AuthResponseDTO login(LoginRequestDTO request);

        PersonaResponseDTO verifyToken(String token);

        AuthResponseDTO validarCodigo(String email, String code);

        AuthResponseDTO reenviarCodigo(String email);
}

