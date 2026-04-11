package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.GoogleUserInfoDTO;

public interface GoogleIdTokenService {
    GoogleUserInfoDTO validarToken(String idToken);
}