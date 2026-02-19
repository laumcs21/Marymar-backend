package com.marymar.app.business.DTO;

public class LoginRequestDTO {

    private String email;
    private String contrasena;

    public LoginRequestDTO() {}

    public LoginRequestDTO(String email, String contrasena) {
        this.email = email;
        this.contrasena = contrasena;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}

