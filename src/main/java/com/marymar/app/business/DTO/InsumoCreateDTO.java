package com.marymar.app.business.DTO;

public class InsumoCreateDTO {

    private String nombre;
    private String unidad; // unidades, kg, litros

    public InsumoCreateDTO() {}

    public InsumoCreateDTO(String nombre, String unidad) {
        this.nombre = nombre;
        this.unidad = unidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }
}

