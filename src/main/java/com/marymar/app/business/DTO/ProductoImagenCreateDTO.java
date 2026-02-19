package com.marymar.app.business.DTO;

public class ProductoImagenCreateDTO {

    private Long productoId;
    private Integer orden;
    private Boolean principal;

    public ProductoImagenCreateDTO() {}

    public ProductoImagenCreateDTO(Long productoId, Integer orden, Boolean principal) {
        this.productoId = productoId;
        this.orden = orden;
        this.principal = principal;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }
}

