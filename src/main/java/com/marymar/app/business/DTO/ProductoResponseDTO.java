package com.marymar.app.business.DTO;

import java.math.BigDecimal;
import java.util.List;

public class ProductoResponseDTO {

    private Long id;
    private String nombre;
    private BigDecimal precio;
    private String descripcion;

    private Long categoriaId;
    private String categoriaNombre;

    private boolean activo;

    private List<String> imagenesUrls;
    private String imagenPrincipal;

    public ProductoResponseDTO() {}

    public ProductoResponseDTO(Long id,
                               String nombre,
                               BigDecimal precio,
                               String descripcion,
                               Long categoriaId,
                               String categoriaNombre,
                               boolean activo,
                               List<String> imagenesUrls,
                               String imagenPrincipal) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.activo = activo;
        this.imagenesUrls = imagenesUrls;
        this.imagenPrincipal = imagenPrincipal;
    }

    public ProductoResponseDTO(Long id,
                               String nombre,
                               BigDecimal precio,
                               String descripcion,
                               Long categoriaId,
                               String categoriaNombre,
                               boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.activo = activo;

    }

    public Long getId() { return id; }

    public String getNombre() { return nombre; }

    public BigDecimal getPrecio() { return precio; }

    public String getDescripcion() { return descripcion; }

    public Long getCategoriaId() { return categoriaId; }

    public String getCategoriaNombre() { return categoriaNombre; }

    public boolean isActivo() { return activo; }

    public List<String> getImagenesUrls() { return imagenesUrls; }

    public String getImagenPrincipal() { return imagenPrincipal; }
}