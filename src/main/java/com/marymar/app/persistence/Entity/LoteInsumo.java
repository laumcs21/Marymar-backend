package com.marymar.app.persistence.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lote_insumo")
public class LoteInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @Column(nullable = false)
    private Integer cantidadInicial;

    @Column(nullable = false)
    private Integer cantidadDisponible;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaIngreso;

    @Column(nullable = false)
    private LocalDateTime fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UbicacionInventario ubicacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoLote estado;

    public LoteInsumo() {
    }

    public LoteInsumo(Insumo insumo,
                      Integer cantidadInicial,
                      Integer cantidadDisponible,
                      LocalDateTime fechaIngreso,
                      LocalDateTime fechaVencimiento,
                      UbicacionInventario ubicacion,
                      EstadoLote estado) {
        this.insumo = insumo;
        this.cantidadInicial = cantidadInicial;
        this.cantidadDisponible = cantidadDisponible;
        this.fechaIngreso = fechaIngreso;
        this.fechaVencimiento = fechaVencimiento;
        this.ubicacion = ubicacion;
        this.estado = estado;
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaIngreso == null) {
            this.fechaIngreso = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoLote.ACTIVO;
        }
        if (this.cantidadDisponible == null) {
            this.cantidadDisponible = this.cantidadInicial;
        }
    }

    public Long getId() {
        return id;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
    }

    public Integer getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(Integer cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    public Integer getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(Integer cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public UbicacionInventario getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(UbicacionInventario ubicacion) {
        this.ubicacion = ubicacion;
    }

    public EstadoLote getEstado() {
        return estado;
    }

    public void setEstado(EstadoLote estado) {
        this.estado = estado;
    }

    public void setId(Long id) {
        this.id = id;
    }
}