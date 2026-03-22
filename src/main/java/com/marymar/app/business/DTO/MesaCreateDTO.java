package com.marymar.app.business.DTO;

public class MesaCreateDTO {

    private Integer numero;
    private Integer capacidad;

    public MesaCreateDTO() {
    }

    public MesaCreateDTO(Integer numero, Integer capacidad) {
        this.numero = numero;
        this.capacidad = capacidad;
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
}