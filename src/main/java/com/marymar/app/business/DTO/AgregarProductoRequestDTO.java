package com.marymar.app.business.DTO;

public class AgregarProductoRequestDTO {

    private Long productoId;
    private int cantidad;
    private String nombrePersona;
    private String observacion;

    public AgregarProductoRequestDTO(int cantidad, String nombrePersona, String observacion, Long productoId) {
        this.cantidad = cantidad;
        this.nombrePersona = nombrePersona;
        this.observacion = observacion;
        this.productoId = productoId;
    }

    public AgregarProductoRequestDTO() {
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getNombrePersona() {
        return nombrePersona;
    }

    public void setNombrePersona(String nombrePersona) {
        this.nombrePersona = nombrePersona;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }
}
