package com.marymar.app.persistence.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "insumo")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String unidad;

    public Insumo() {}

    public Insumo(String nombre, String unidad) {
        this.nombre = nombre;
        this.unidad = unidad;
    }

    public Long getId() { return id; }

    public String getNombre() { return nombre; }

    public String getUnidad() { return unidad; }
}

