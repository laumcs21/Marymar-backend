package com.marymar.app.business.DTO;

import java.time.LocalDate;

public class PersonaCreateDTO {

    private String numeroIdentificacion;
    private String nombre;
    private String email;
    private String contrasena;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String rol; // aquí puede quedarse como String
    private String direccionEnvio; // opcional
    private Double salario; // opcional

    public PersonaCreateDTO() {}

    public PersonaCreateDTO(String numeroIdentificacion,
                            String nombre,
                            String email,
                            String contrasena,
                            String telefono,
                            LocalDate fechaNacimiento,
                            String rol,
                            String direccionEnvio,
                            Double salario) {

        this.numeroIdentificacion = numeroIdentificacion;
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.rol = rol;
        this.direccionEnvio = direccionEnvio;
        this.salario = salario;
    }

    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public Double getSalario() { return salario; }
    public void setSalario(Double salario) {
        this.salario = salario;
    }
}



