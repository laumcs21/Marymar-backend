package com.marymar.app.business.DTO;

import com.marymar.app.persistence.Entity.EstadoMesa;

public class MesaResponseDTO {

    private Long id;
    private Integer numero;
    private Integer capacidad;
    private EstadoMesa estado;
    private Long meseroAsignadoId;
    private String meseroAsignadoNombre;
    private boolean activa;

    public MesaResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public EstadoMesa getEstado() {
        return estado;
    }

    public void setEstado(EstadoMesa estado) {
        this.estado = estado;
    }

    public Long getMeseroAsignadoId() {
        return meseroAsignadoId;
    }

    public void setMeseroAsignadoId(Long meseroAsignadoId) {
        this.meseroAsignadoId = meseroAsignadoId;
    }

    public String getMeseroAsignadoNombre() {
        return meseroAsignadoNombre;
    }

    public void setMeseroAsignadoNombre(String meseroAsignadoNombre) {
        this.meseroAsignadoNombre = meseroAsignadoNombre;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}