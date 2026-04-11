package com.marymar.app.business.DTO;

public class GoogleUserInfoDTO {

    private String sub;
    private String email;
    private String nombre;

    public GoogleUserInfoDTO() {
    }

    public GoogleUserInfoDTO(String sub, String email, String nombre) {
        this.sub = sub;
        this.email = email;
        this.nombre = nombre;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}