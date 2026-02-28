package com.marymar.app.business.DTO;

public class CategoriaResponseDTO {

    private Long id;
    private String nombre;
    private Long cantidadProductos;

    public CategoriaResponseDTO() {}

    public CategoriaResponseDTO(Long id, String nombre, Long cantidadProductos) {
        this.id = id;
        this.nombre = nombre;
        this.cantidadProductos = cantidadProductos;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Long getCantidadProductos() {
        return cantidadProductos;
    }
}