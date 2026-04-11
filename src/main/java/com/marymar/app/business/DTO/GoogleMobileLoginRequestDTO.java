package com.marymar.app.business.DTO;

public class GoogleMobileLoginRequestDTO {

    private String idToken;
    private String captchaToken;

    /**
     * Acción esperada del reCAPTCHA Android.
     * Ej: GOOGLE_LOGIN
     */
    private String captchaAction;

    public GoogleMobileLoginRequestDTO() {
    }

    public GoogleMobileLoginRequestDTO(String idToken, String captchaToken, String captchaAction) {
        this.idToken = idToken;
        this.captchaToken = captchaToken;
        this.captchaAction = captchaAction;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }

    public String getCaptchaAction() {
        return captchaAction;
    }

    public void setCaptchaAction(String captchaAction) {
        this.captchaAction = captchaAction;
    }
}