package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.GoogleMobileLoginRequestDTO;
import com.marymar.app.business.DTO.LoginRequestDTO;
import com.marymar.app.business.DTO.Auth.AuthResponseDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.DTO.RegisterRequestDTO;

public interface AuthService {

        // Se mantienen para compatibilidad interna
        AuthResponseDTO register(RegisterRequestDTO request);
        AuthResponseDTO login(LoginRequestDTO request);

        // Nuevos overloads para usar userAgent / userIp sin romper nada existente
        AuthResponseDTO register(RegisterRequestDTO request, String userAgent, String userIp);
        AuthResponseDTO login(LoginRequestDTO request, String userAgent, String userIp);

        AuthResponseDTO loginWithGoogleMobile(
                GoogleMobileLoginRequestDTO request,
                String userAgent,
                String userIp
        );

        PersonaResponseDTO verifyToken(String token);

        AuthResponseDTO validarCodigo(String email, String code);

        AuthResponseDTO reenviarCodigo(String email);
}