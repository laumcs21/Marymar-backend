package com.marymar.app.business.DTO;

import com.marymar.app.persistence.Entity.Rol;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PersonaResponseDTO {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private Rol rol;
    private String direccionEnvio;
    private Double salario;
    private String numeroIdentificacion;
    private boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PersonaResponseDTO() {}

    public PersonaResponseDTO(Long id,
                              String nombre,
                              String email,
                              String telefono,
                              LocalDate fechaNacimiento,
                              Rol rol,
                              String direccionEnvio,
                              Double salario,
                              String numeroIdentificacion,
                              boolean activo,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {

        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.rol = rol;
        this.direccionEnvio = direccionEnvio;
        this.salario = salario;
        this.numeroIdentificacion = numeroIdentificacion;
        this.activo = activo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public String getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public Double getSalario() { return salario; }
    public void setSalario(Double salario) {
        this.salario = salario;
    }

    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

