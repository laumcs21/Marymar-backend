package com.marymar.app.business.DTO;

public class InventarioCreateDTO {

    private Long insumoId;
    private Integer stock;

    public InventarioCreateDTO() {}

    public InventarioCreateDTO(Long insumoId, Integer stock) {
        this.insumoId = insumoId;
        this.stock = stock;
    }

    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
