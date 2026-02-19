package com.marymar.app.business.DTO;

public class CategoriaCreateDTO {

    private String nombre;

    public CategoriaCreateDTO() {}

    public CategoriaCreateDTO(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

