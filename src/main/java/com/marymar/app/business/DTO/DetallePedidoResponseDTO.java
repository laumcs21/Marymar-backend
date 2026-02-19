package com.marymar.app.business.DTO;

import java.math.BigDecimal;

public class DetallePedidoResponseDTO {

    private Long id;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public DetallePedidoResponseDTO() {}

    public DetallePedidoResponseDTO(Long id,
                                    String productoNombre,
                                    Integer cantidad,
                                    BigDecimal precioUnitario,
                                    BigDecimal subtotal) {
        this.id = id;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
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
}
