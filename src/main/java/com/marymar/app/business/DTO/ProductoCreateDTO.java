package com.marymar.app.business.DTO;

import java.math.BigDecimal;

public class ProductoCreateDTO {

    private String nombre;
    private BigDecimal precio;
    private String descripcion;
    private Long categoriaId;

    public ProductoCreateDTO() {}

    public ProductoCreateDTO(String nombre, BigDecimal precio, Long categoriaId, String descripcion) {
        this.nombre = nombre;
        this.precio = precio;
        this.categoriaId = categoriaId;
        this.descripcion = descripcion;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

