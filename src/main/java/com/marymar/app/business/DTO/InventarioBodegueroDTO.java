package com.marymar.app.business.DTO;

public class InventarioBodegueroDTO {

    private Long inventarioId;
    private Long insumoId;
    private String insumoNombre;

    private int stockTotal;
    private int stockCocina;
    private int stockBodega;

    public InventarioBodegueroDTO(Long inventarioId, Long insumoId, String insumoNombre,
                                  int stockTotal, int stockCocina, int stockBodega) {
        this.inventarioId = inventarioId;
        this.insumoId = insumoId;
        this.insumoNombre = insumoNombre;
        this.stockTotal = stockTotal;
        this.stockCocina = stockCocina;
        this.stockBodega = stockBodega;
    }

    public Long getInventarioId() { return inventarioId; }
    public Long getInsumoId() { return insumoId; }
    public String getInsumoNombre() { return insumoNombre; }
    public int getStockTotal() { return stockTotal; }
    public int getStockCocina() { return stockCocina; }
    public int getStockBodega() { return stockBodega; }
}
