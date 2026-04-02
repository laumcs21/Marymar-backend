package com.marymar.app.business.DTO;

import java.math.BigDecimal;

public class DetallePedidoResponseDTO {

    private Long id;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private Long productoId;

    public DetallePedidoResponseDTO() {}

    public DetallePedidoResponseDTO(Long id,
                                    String productoNombre,
                                    Integer cantidad,
                                    BigDecimal precioUnitario,
                                    BigDecimal subtotal,
                                    Long productoId) {
        this.id = id;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.productoId = productoId;
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

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
