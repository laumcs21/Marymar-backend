package com.marymar.app.business.DTO;

public class CategoriaResponseDTO {

    private Long id;
    private String nombre;

    public CategoriaResponseDTO() {}

    public CategoriaResponseDTO(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}

