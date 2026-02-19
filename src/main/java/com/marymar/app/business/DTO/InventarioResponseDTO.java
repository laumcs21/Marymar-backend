package com.marymar.app.business.DTO;

import java.time.LocalDateTime;

public class InventarioResponseDTO {

    private Long id;
    private Long insumoId;
    private String insumoNombre;
    private String unidad;
    private Integer stock;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    public InventarioResponseDTO() {}

    public InventarioResponseDTO(Long id,
                                 Long insumoId,
                                 String insumoNombre,
                                 String unidad,
                                 Integer stock,
                                 LocalDateTime fechaCreacion,
                                 LocalDateTime fechaModificacion) {
        this.id = id;
        this.insumoId = insumoId;
        this.insumoNombre = insumoNombre;
        this.unidad = unidad;
        this.stock = stock;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
    }

    public Long getId() { return id; }

    public Long getInsumoId() { return insumoId; }

    public String getInsumoNombre() { return insumoNombre; }

    public String getUnidad() { return unidad; }

    public Integer getStock() { return stock; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
}
