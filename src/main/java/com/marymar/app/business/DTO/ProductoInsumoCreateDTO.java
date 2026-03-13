package com.marymar.app.business.DTO;

public class ProductoInsumoCreateDTO {

    private Long productoId;
    private Long insumoId;
    private Integer cantidad;

    public ProductoInsumoCreateDTO() {}

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}