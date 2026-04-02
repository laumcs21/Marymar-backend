package com.marymar.app.persistence.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consumo_inventario")
public class ConsumoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pedidoId;
    private Long insumoId;
    private Integer cantidad;
    private LocalDateTime fecha;

    public ConsumoInventario() {}

    public ConsumoInventario(Long pedidoId, Long insumoId, Integer cantidad, LocalDateTime fecha) {
        this.pedidoId = pedidoId;
        this.insumoId = insumoId;
        this.cantidad = cantidad;
        this.fecha = fecha;
    }

    // getters y setters
    public Long getId() { return id; }

    public Long getPedidoId() { return pedidoId; }

    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }

    public Long getInsumoId() { return insumoId; }

    public void setInsumoId(Long insumoId) { this.insumoId = insumoId; }

    public Integer getCantidad() { return cantidad; }

    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public LocalDateTime getFecha() { return fecha; }

    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}