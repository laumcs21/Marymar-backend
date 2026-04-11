package com.marymar.app.business.DTO;

public class LoginRequestDTO {

    private String email;
    private String contrasena;
    private String captchaToken;

    /**
     * WEB o ANDROID
     * Si viene null o vacío, se mantiene el comportamiento actual (WEB).
     */
    private String captchaClient;

    /**
     * Acción esperada para Android reCAPTCHA Enterprise.
     * Ej: LOGIN
     */
    private String captchaAction;

    public LoginRequestDTO() {}

    public LoginRequestDTO(String email, String contrasena) {
        this.email = email;
        this.contrasena = contrasena;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }

    public String getCaptchaClient() {
        return captchaClient;
    }

    public void setCaptchaClient(String captchaClient) {
        this.captchaClient = captchaClient;
    }

    public String getCaptchaAction() {
        return captchaAction;
    }

    public void setCaptchaAction(String captchaAction) {
        this.captchaAction = captchaAction;
    }
}