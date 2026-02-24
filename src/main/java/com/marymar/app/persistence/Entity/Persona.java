package com.marymar.app.persistence.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "persona")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_identificacion", unique = true, nullable = true)
    private String numeroIdentificacion;

    private String nombre;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String contrasena;

    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(name = "direccion_envio")
    private String direccionEnvio; // OPCIONAL

    private Double salario; // OPCIONAL

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================
    // CONSTRUCTOR VACÍO
    // =========================
    public Persona() {
    }

    // =========================
    // CONSTRUCTOR COMPLETO
    // =========================
    public Persona(Long id,
                   String numeroIdentificacion,
                   String nombre,
                   String email,
                   String contrasena,
                   String telefono,
                   LocalDate fechaNacimiento,
                   Rol rol,
                   String direccionEnvio,
                   Double salario,
                   boolean activo) {

        this.id = id;
        this.numeroIdentificacion = numeroIdentificacion;
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.rol = rol;
        this.direccionEnvio = direccionEnvio;
        this.salario = salario;
        this.activo = activo;
    }


    // =========================
    // BUILDER MANUAL
    // =========================

    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {

        private String numeroIdentificacion;
        private String nombre;
        private String email;
        private String contrasena;
        private String telefono;
        private LocalDate fechaNacimiento;
        private Rol rol;
        private String direccionEnvio;
        private Double salario;
        private boolean activo = true;

        public Builder numeroIdentificacion(String numeroIdentificacion) {
            this.numeroIdentificacion = numeroIdentificacion;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder contrasena(String contrasena) {
            this.contrasena = contrasena;
            return this;
        }

        public Builder telefono(String telefono) {
            this.telefono = telefono;
            return this;
        }

        public Builder fechaNacimiento(LocalDate fechaNacimiento) {
            this.fechaNacimiento = fechaNacimiento;
            return this;
        }

        public Builder rol(Rol rol) {
            this.rol = rol;
            return this;
        }

        // OPCIONALES
        public Builder direccionEnvio(String direccionEnvio) {
            this.direccionEnvio = direccionEnvio;
            return this;
        }

        public Builder salario(Double salario) {
            this.salario = salario;
            return this;
        }

        public Builder activo(boolean activo) {
            this.activo = activo;
            return this;
        }

        public Persona build() {
            Persona persona = new Persona();
            persona.numeroIdentificacion = this.numeroIdentificacion;
            persona.nombre = this.nombre;
            persona.email = this.email;
            persona.contrasena = this.contrasena;
            persona.telefono = this.telefono;
            persona.fechaNacimiento = this.fechaNacimiento;
            persona.rol = this.rol;
            persona.direccionEnvio = this.direccionEnvio;
            persona.salario = this.salario;
            persona.activo = this.activo;
            return persona;
        }
    }

    // =========================
    // MÉTODOS JPA
    // =========================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================
    // GETTERS Y SETTERS
    // =========================

    public Long getId() { return id; }

    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

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
    public void setSalario(Double salario) { this.salario = salario; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
