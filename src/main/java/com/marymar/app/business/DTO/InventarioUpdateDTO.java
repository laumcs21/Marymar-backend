package com.marymar.app.business.DTO;

public class InventarioUpdateDTO {

    private Integer stock;

    public InventarioUpdateDTO() {}

    public InventarioUpdateDTO(Integer stock) {
        this.stock = stock;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}

