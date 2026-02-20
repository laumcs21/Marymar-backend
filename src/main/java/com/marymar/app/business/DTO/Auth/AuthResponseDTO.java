package com.marymar.app.business.DTO.Auth;

import com.marymar.app.persistence.Entity.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder

public class AuthResponseDTO {
        private String token;
        private String nombre;
        private Rol rol;
        private String mensaje;
        private boolean requires2FA;


    public AuthResponseDTO(String nombre, Rol rol, String token, boolean requires2FA) {
        this.nombre = nombre;
        this.rol = rol;
        this.token = token;
        this.requires2FA = requires2FA;

    }

    public AuthResponseDTO(String mensaje) {
        this.mensaje = mensaje;
    }

    public AuthResponseDTO() {
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isRequires2FA() {
        return requires2FA;
    }
}
