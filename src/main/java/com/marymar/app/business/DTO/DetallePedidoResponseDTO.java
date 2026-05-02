package com.marymar.app.business.DTO;

import java.math.BigDecimal;

public class DetallePedidoResponseDTO {

    private Long id;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private Long productoId;
    private String nombrePersona;
    private String observacion;

    public DetallePedidoResponseDTO() {}

    public DetallePedidoResponseDTO(Long id,
                                    String productoNombre,
                                    Integer cantidad,
                                    BigDecimal precioUnitario,
                                    BigDecimal subtotal,
                                    Long productoId,
                                    String nombrePersona,
                                    String observacion) {
        this.id = id;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.productoId = productoId;
        this.nombrePersona = nombrePersona;
        this.observacion = observacion;
    }

    public Long getId() {
        return id;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public Long getProductoId() {
        return productoId;
    }

    public String getNombrePersona() {
        return nombrePersona;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public void setNombrePersona(String nombrePersona) {
        this.nombrePersona = nombrePersona;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}