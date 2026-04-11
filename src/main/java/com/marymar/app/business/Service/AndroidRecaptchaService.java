package com.marymar.app.business.Service;

public interface AndroidRecaptchaService {

    boolean validarCaptchaAndroid(
            String token,
            String expectedAction,
            String userAgent,
            String userIp
    );
}