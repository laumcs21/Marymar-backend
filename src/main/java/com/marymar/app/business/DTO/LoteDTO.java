package com.marymar.app.business.DTO;

import java.time.LocalDateTime;

public class LoteDTO {

    private Long id;
    private int cantidadInicial;
    private int cantidadDisponible;
    private String ubicacion;
    private LocalDateTime fechaIngreso;
    private LocalDateTime fechaVencimiento;

    public LoteDTO(Long id, int cantidadInicial, int cantidadDisponible,
                   String ubicacion,
                   LocalDateTime fechaIngreso,
                   LocalDateTime fechaVencimiento) {

        this.id = id;
        this.cantidadInicial = cantidadInicial;
        this.cantidadDisponible = cantidadDisponible;
        this.ubicacion = ubicacion;
        this.fechaIngreso = fechaIngreso;
        this.fechaVencimiento = fechaVencimiento;
    }

    public Long getId() { return id; }
    public int getCantidadInicial() { return cantidadInicial; }
    public int getCantidadDisponible() { return cantidadDisponible; }
    public String getUbicacion() { return ubicacion; }
    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public LocalDateTime getFechaVencimiento() { return fechaVencimiento; }
}