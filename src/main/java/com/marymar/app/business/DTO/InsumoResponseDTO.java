package com.marymar.app.business.DTO;

public class InsumoResponseDTO {

    private Long id;
    private String nombre;
    private String unidad;

    public InsumoResponseDTO() {}

    public InsumoResponseDTO(Long id, String nombre, String unidad) {
        this.id = id;
        this.nombre = nombre;
        this.unidad = unidad;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUnidad() {
        return unidad;
    }
}
